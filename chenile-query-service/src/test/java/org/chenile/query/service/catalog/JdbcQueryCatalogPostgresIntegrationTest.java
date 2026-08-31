package org.chenile.query.service.catalog;

import org.apache.ibatis.session.SqlSessionFactory;
import org.chenile.configuration.query.service.QueryCatalogJdbcProperties;
import org.chenile.query.service.impl.QueryDefinitions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import tools.jackson.databind.json.JsonMapper;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Verifies the shipped catalog schema and mapper loading against the production database dialect. */
@Testcontainers(disabledWithoutDocker = true)
class JdbcQueryCatalogPostgresIntegrationTest {
	@Container
	static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine");

	private static DataSource catalogDataSource;
	private static JdbcTemplate jdbc;

	@BeforeAll
	static void createCatalogSchema() {
		catalogDataSource = new DriverManagerDataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
		DatabasePopulatorUtils.execute(new ResourceDatabasePopulator(
				new ClassPathResource("chenile-query-catalog-schema.sql")), catalogDataSource);
		jdbc = new JdbcTemplate(catalogDataSource);
	}

	@AfterEach
	void clearCatalog() {
		jdbc.update("delete from chenile_query_definition_source");
		jdbc.update("delete from chenile_query_mapper_source");
	}

	@Test
	void loadsPostgresCatalogAndExecutesGlobalTenantQualifiedAndClasspathFallbackMappers() throws Exception {
		insertMapper("Student", mapperXml("Student", "select 42 as id"));
		insertMapper("tenant1.Student", mapperXml("tenant1.Student", "select 84 as id"));
		insertDefinition("__base__", "students", "{\"id\":\"Student.getAll\",\"name\":\"students\"}");
		insertDefinition("tenant1", "students", "{\"id\":\"tenant1.Student.getAll\",\"name\":\"students\",\"tenantId\":\"tenant1\"}");

		QueryCatalogJdbcProperties properties = new QueryCatalogJdbcProperties();
		JdbcQueryCatalog catalog = new JdbcQueryCatalog(catalogDataSource, properties, JsonMapper.builder().build());
		Resource fallbackMapper = resource(mapperXml("Fallback", "select 11 as id"));
		QueryCatalogSnapshot snapshot = QueryCatalogSnapshot.databasePreferred(
				new Resource[] { fallbackMapper }, new Resource[0], catalog.loadMappers(), catalog.loadDefinitions());

		SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
		factoryBean.setDataSource(tenantRoutingDataSource(catalogDataSource));
		factoryBean.setMapperLocations(snapshot.mapperFiles());
		SqlSessionFactory factory = factoryBean.getObject();
		snapshot.validateDatabaseDefinitions(factory.getConfiguration());

		QueryDefinitions definitions = new QueryDefinitions(snapshot.definitionFiles(), snapshot.databaseDefinitions(),
				JsonMapper.builder().build());
		assertEquals("tenant1.Student.getAll", definitions.retrieve("students", "tenant1").getId());
		assertEquals("Student.getAll", definitions.retrieve("students", "tenant2").getId());
		try (var session = factory.openSession()) {
			assertEquals(84, resultId(session.selectOne("tenant1.Student.getAll")));
			assertEquals(42, resultId(session.selectOne("Student.getAll")));
			assertEquals(11, resultId(session.selectOne("Fallback.getAll")));
		}
	}

	private static DataSource tenantRoutingDataSource(DataSource tenantDataSource) {
		AbstractRoutingDataSource routingDataSource = new AbstractRoutingDataSource() {
			@Override protected Object determineCurrentLookupKey() { return "tenant1"; }
		};
		Map<Object, Object> dataSources = new LinkedHashMap<>();
		dataSources.put("tenant1", tenantDataSource);
		routingDataSource.setTargetDataSources(dataSources);
		routingDataSource.setDefaultTargetDataSource(tenantDataSource);
		routingDataSource.afterPropertiesSet();
		return routingDataSource;
	}

	private void insertMapper(String namespace, String xml) {
		jdbc.update("insert into chenile_query_mapper_source (namespace, mapper_xml, checksum) values (?, ?, ?)",
			namespace, xml, "test-checksum");
	}

	private void insertDefinition(String scope, String queryName, String definition) {
		jdbc.update("insert into chenile_query_definition_source (scope_key, query_name, definition_json, checksum) values (?, ?, ?, ?)",
				scope, queryName, definition, "test-checksum");
	}

	private static int resultId(Object result) {
		@SuppressWarnings("unchecked")
		Map<String, Object> row = (Map<String, Object>) result;
		return ((Number) row.values().iterator().next()).intValue();
	}

	private static Resource resource(String content) {
		return new ByteArrayResource(content.getBytes(StandardCharsets.UTF_8));
	}

	private static String mapperXml(String namespace, String select) {
		return """
				<?xml version="1.0" encoding="UTF-8" ?>
				<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "https://mybatis.org/dtd/mybatis-3-mapper.dtd">
				<mapper namespace="%s"><select id="getAll" resultType="map">%s</select></mapper>
				""".formatted(namespace, select);
	}
}
