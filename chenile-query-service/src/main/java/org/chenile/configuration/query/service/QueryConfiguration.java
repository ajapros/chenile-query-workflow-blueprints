package org.chenile.configuration.query.service;

import java.io.IOException;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.function.Function;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.chenile.query.service.catalog.JdbcQueryCatalog;
import org.chenile.query.service.catalog.QueryCatalogSnapshot;
import org.chenile.core.context.ChenileExchange;
import org.chenile.core.context.ContextContainer;
import org.chenile.query.model.QueryMetadata;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import tools.jackson.databind.json.JsonMapper;
import org.chenile.query.service.QueryStore;
import org.chenile.query.service.SearchService;
import org.chenile.query.service.impl.MybatisQueryExecutionProvider;
import org.chenile.query.service.impl.NamedQueryServiceSpringMybatisImpl;
import org.chenile.query.service.impl.QueryExecutionProvider;
import org.chenile.query.service.impl.QueryDefinitions;
import org.chenile.query.service.interceptor.QueryUserFilterInterceptor;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * Registers query beans in Spring
 */
@Configuration
public class QueryConfiguration {

	@Value("${query.mapperFiles:}")
	private String mapperFilePatterns;
	
	@Value("${query.definitionFiles:}")
	private String queryDefinitionFilePatterns;
	
	@Bean
	@ConfigurationProperties(prefix = "query.catalog.jdbc")
	QueryCatalogJdbcProperties queryCatalogJdbcProperties() {
		return new QueryCatalogJdbcProperties();
	}

	@Bean("queryJsonMapper")
	JsonMapper queryJsonMapper() {
		return JsonMapper.builder().build();
	}

	@Bean("queryCatalogSnapshot")
	QueryCatalogSnapshot queryCatalogSnapshot(@Autowired QueryCatalogJdbcProperties catalogProperties,
			@Autowired ApplicationContext applicationContext,
			@Autowired @Qualifier("queryJsonMapper") JsonMapper jsonMapper) {
		Resource[] mapperFiles = resolveResources(mapperFilePatterns, "query.mapperFiles");
		Resource[] queryDefinitionFiles = resolveResources(queryDefinitionFilePatterns, "query.definitionFiles");
		if (!catalogProperties.isEnabled()) {
			if (queryDefinitionFiles.length == 0) {
				throw new IllegalStateException("query.definitionFiles is required when the JDBC query catalog is disabled");
			}
			return QueryCatalogSnapshot.classpathOnly(mapperFiles, queryDefinitionFiles);
		}
		String beanName = catalogProperties.getDataSource();
		if (beanName == null || beanName.isBlank()) {
			throw new IllegalStateException("query.catalog.jdbc.dataSource is required when the JDBC catalog is enabled");
		}
		if (catalogProperties.getBaseScope() == null || catalogProperties.getBaseScope().isBlank()) {
			throw new IllegalStateException("query.catalog.jdbc.baseScope is required when the JDBC catalog is enabled");
		}
		DataSource catalogDataSource;
		try {
			catalogDataSource = applicationContext.getBean(beanName, DataSource.class);
		} catch (Exception e) {
			throw new IllegalStateException("JDBC query catalog datasource bean '" + beanName + "' is unavailable", e);
		}
		JdbcQueryCatalog catalog = new JdbcQueryCatalog(catalogDataSource, catalogProperties, jsonMapper);
		return QueryCatalogSnapshot.databasePreferred(mapperFiles, queryDefinitionFiles,
				catalog.loadMappers(), catalog.loadDefinitions());
	}

	private Resource[] resolveResources(String patterns, String propertyName) {
		if (patterns == null || patterns.isBlank()) {
			return new Resource[0];
		}
		try {
			PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
			return Arrays.stream(patterns.split(","))
					.map(String::trim)
					.filter(pattern -> !pattern.isEmpty())
					.flatMap(pattern -> Arrays.stream(resourcesFor(resolver, pattern, propertyName)))
					.toArray(Resource[]::new);
		} catch (IllegalStateException exception) {
			throw exception;
		}
	}

	private Resource[] resourcesFor(PathMatchingResourcePatternResolver resolver, String pattern, String propertyName) {
		try {
			return resolver.getResources(pattern);
		} catch (IOException exception) {
			throw new IllegalStateException("Unable to resolve " + propertyName + " resource pattern '" + pattern + "'", exception);
		}
	}

	@Bean("queryDefinitions") QueryDefinitions queryDefinitions(
			@Autowired @Qualifier("queryCatalogSnapshot") QueryCatalogSnapshot catalogSnapshot,
			@Autowired @Qualifier("queryJsonMapper") JsonMapper jsonMapper) throws IOException{
		return new QueryDefinitions(catalogSnapshot.definitionFiles(), catalogSnapshot.databaseDefinitions(), jsonMapper);
	}

    @Bean
    @ConfigurationProperties(prefix = "query")
    QueryDatasourcesProperties queryDatasourcesProperties() {
		return new QueryDatasourcesProperties();
	}

	@Bean
	@ConfigurationProperties(prefix = "query.pagination")
	QueryPaginationProperties queryPaginationProperties() {
		return new QueryPaginationProperties();
	}

	@Bean
	QueryTenantResolver queryTenantResolver(@Autowired QueryDatasourcesProperties properties,
			@Autowired ContextContainer contextContainer) {
		return new QueryTenantResolver(properties, contextContainer);
	}

    @Bean("queryTargetDataSources")
	@ConditionalOnProperty(name = "query.mybatis.enabled", havingValue = "true", matchIfMissing = true)
    Map<String, DataSource> queryTargetDataSources(@Autowired QueryDatasourcesProperties properties) {
		Map<String, DataSource> targetDataSources = new LinkedHashMap<>();
		for (Map.Entry<String, Map<String, String>> entry : properties.getDatasources().entrySet()) {
			java.util.Properties hikariProps = new java.util.Properties();
			for (Map.Entry<String, String> prop : entry.getValue().entrySet()) {
				if ("type".equals(prop.getKey())) {
					continue;
				}
				hikariProps.setProperty(prop.getKey(), prop.getValue());
			}
			HikariConfig hikariConfig = new HikariConfig(hikariProps);
			HikariDataSource dataSource = new HikariDataSource(hikariConfig);
			targetDataSources.put(entry.getKey(), dataSource);
		}
		return targetDataSources;
	}

    @Bean("queryDatasource")
	@ConditionalOnProperty(name = "query.mybatis.enabled", havingValue = "true", matchIfMissing = true)
    DataSource queryDataSource(@Autowired @Qualifier("queryTargetDataSources") Map<String, DataSource> targetDataSources,
							   @Autowired QueryDatasourcesProperties properties,
							   @Autowired QueryTenantResolver queryTenantResolver) {
		if (targetDataSources.isEmpty()) {
			throw new IllegalStateException("query.datasources is empty or not configured");
		}
		String defaultTenantId = queryTenantResolver.getDefaultTenantId();
		if (defaultTenantId != null && !targetDataSources.containsKey(defaultTenantId)) {
			throw new IllegalStateException(
					"query.defaultTenantId '" + defaultTenantId + "' is not present in query.datasources");
		}
		AbstractRoutingDataSource routingDataSource = new AbstractRoutingDataSource() {
			@Override
			protected Object determineCurrentLookupKey() {
				return queryTenantResolver.resolveTenant();
			}
		};
		Map<Object, Object> target = new LinkedHashMap<>();
		for (Map.Entry<String, DataSource> entry : targetDataSources.entrySet()) {
			target.put(entry.getKey(), entry.getValue());
		}
		routingDataSource.setTargetDataSources(target);
		routingDataSource.setLenientFallback(false);
		if (defaultTenantId != null) {
			routingDataSource.setDefaultTargetDataSource(targetDataSources.get(defaultTenantId));
		}
		return routingDataSource;
	}

    @Bean
	@ConditionalOnProperty(name = "query.mybatis.enabled", havingValue = "true", matchIfMissing = true)
    SqlSessionFactory sqlSessionFactory(@Autowired @Qualifier("queryDatasource") DataSource queryDataSource,
			@Autowired @Qualifier("queryCatalogSnapshot") QueryCatalogSnapshot catalogSnapshot)
            throws Exception {
		SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
		factoryBean.setDataSource(queryDataSource);
		factoryBean.setMapperLocations(catalogSnapshot.mapperFiles());
		SqlSessionFactory sqlSessionFactory = factoryBean.getObject();
		if (sqlSessionFactory == null) {
			throw new IllegalStateException("Unable to create MyBatis SqlSessionFactory");
		}
		catalogSnapshot.validateDatabaseDefinitions(sqlSessionFactory.getConfiguration());
		return sqlSessionFactory;
	}

	@Bean("mybatisQueryExecutionProvider")
	@ConditionalOnProperty(name = "query.mybatis.enabled", havingValue = "true", matchIfMissing = true)
	QueryExecutionProvider mybatisQueryExecutionProvider(@Autowired SqlSessionTemplate sqlSessionTemplate) {
		return new MybatisQueryExecutionProvider(sqlSessionTemplate);
	}

	QueryExecutionProvider queryExecutionProvider(QueryDatasourcesProperties properties,
			List<QueryExecutionProvider> queryExecutionProviders) {
		String provider = properties.getProvider();
		if (provider == null || provider.trim().isEmpty()) {
			provider = MybatisQueryExecutionProvider.PROVIDER_NAME;
		} else {
			provider = provider.trim();
		}
		for (QueryExecutionProvider candidate : queryExecutionProviders) {
			if (candidate.getProviderName().equalsIgnoreCase(provider)) {
				return candidate;
			}
		}
		String supportedProviders = queryExecutionProviders.stream()
				.map(QueryExecutionProvider::getProviderName)
				.collect(Collectors.joining(", "));
		throw new IllegalArgumentException("Unsupported query provider: " + provider
				+ ". Available query providers: " + supportedProviders);
	}

    @Bean
    SearchService<Map<String, Object>> searchService(@Autowired @Qualifier("queryDefinitions")
       QueryStore queryStore, @Autowired QueryPaginationProperties queryPaginationProperties,
	   @Autowired QueryDatasourcesProperties queryDatasourcesProperties,
	   @Autowired QueryTenantResolver queryTenantResolver,
	   @Autowired List<QueryExecutionProvider> queryExecutionProviders) {
		NamedQueryServiceSpringMybatisImpl searchService = new NamedQueryServiceSpringMybatisImpl(queryStore);
		searchService.setPaginationProperties(queryPaginationProperties);
		searchService.setQueryExecutionProvider(queryExecutionProvider(queryDatasourcesProperties, queryExecutionProviders));
		searchService.setQueryTenantResolver(queryTenantResolver);
		return searchService;
	}

    @Bean
	@ConditionalOnProperty(name = "query.mybatis.enabled", havingValue = "true", matchIfMissing = true)
    SqlSessionTemplate sqlSessionTemplate(@Autowired SqlSessionFactory sqlSessionFactory) {
		return new SqlSessionTemplate(sqlSessionFactory);
	}

    @Bean
    QueryUserFilterInterceptor queryUserFilterInterceptor() {
		return new QueryUserFilterInterceptor();
	}

	@Bean
	Function<ChenileExchange,String[]> queryAuthorities(@Autowired QueryDefinitions queryDefinitions,
			@Autowired QueryTenantResolver queryTenantResolver){
		return (exchange) -> {
			String queryName = exchange.getHeader("queryName",String.class);
			if (queryName == null) return null;
			QueryMetadata data = queryDefinitions.retrieve(queryName, queryTenantResolver.resolveTenant());
			if (data == null) return null;
			return data.getAcls();
		};
	}
}
