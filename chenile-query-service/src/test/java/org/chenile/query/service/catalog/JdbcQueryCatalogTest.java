package org.chenile.query.service.catalog;

import org.apache.ibatis.session.SqlSessionFactory;
import org.chenile.configuration.query.service.QueryCatalogJdbcProperties;
import org.chenile.query.service.impl.QueryDefinitions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import tools.jackson.databind.json.JsonMapper;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JdbcQueryCatalogTest {
	private final DataSource dataSource = new DriverManagerDataSource(
			"jdbc:h2:mem:query_catalog;DB_CLOSE_DELAY=-1", "sa", "");
	private final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
	private final QueryCatalogJdbcProperties properties = new QueryCatalogJdbcProperties();

	@BeforeEach
	void setup() {
		jdbcTemplate.execute("drop table if exists chenile_query_mapper_source");
		jdbcTemplate.execute("drop table if exists chenile_query_definition_source");
		jdbcTemplate.execute("create table chenile_query_mapper_source (namespace varchar(512), mapper_xml clob, enabled boolean)");
		jdbcTemplate.execute("create table chenile_query_definition_source (scope_key varchar(255), query_name varchar(255), definition_json clob, enabled boolean)");
	}

	@Test
	void databaseMapperAndDefinitionOverrideClasspathAtStartup() throws Exception {
		jdbcTemplate.update("insert into chenile_query_mapper_source values (?, ?, true)",
				"Catalog.Student", mapperXml("Catalog.Student", "select 7 as id"));
		jdbcTemplate.update("insert into chenile_query_definition_source values (?, ?, ?, true)",
				"__base__", "students", "{\"id\":\"Catalog.Student.getAll\",\"name\":\"students\"}");

		JdbcQueryCatalog catalog = catalog();
		Resource packagedMapper = resource(mapperXml("Catalog.Student", "select 1 as id"));
		QueryCatalogSnapshot snapshot = QueryCatalogSnapshot.databasePreferred(
				new Resource[] { packagedMapper }, new Resource[0], catalog.loadMappers(), catalog.loadDefinitions());

		SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
		factoryBean.setDataSource(dataSource);
		factoryBean.setMapperLocations(snapshot.mapperFiles());
		SqlSessionFactory factory = factoryBean.getObject();
		snapshot.validateDatabaseDefinitions(factory.getConfiguration());

		QueryDefinitions definitions = new QueryDefinitions(snapshot.definitionFiles(), snapshot.databaseDefinitions());
		assertEquals("Catalog.Student.getAll", definitions.retrieve("students").getId());
		try (var session = factory.openSession()) {
			@SuppressWarnings("unchecked")
			java.util.Map<String, Object> row = (java.util.Map<String, Object>) session.selectOne("Catalog.Student.getAll");
			assertEquals(7, ((Number) row.get("ID")).intValue());
		}
	}

	@Test
	void tenantDefinitionOverridesBaseAndOtherTenantsFallBack() throws Exception {
		jdbcTemplate.update("insert into chenile_query_definition_source values (?, ?, ?, true)",
				"__base__", "students", "{\"id\":\"Base.Student.getAll\",\"name\":\"students\"}");
		jdbcTemplate.update("insert into chenile_query_definition_source values (?, ?, ?, true)",
				"tenant1", "students", "{\"id\":\"Tenant.Student.getAll\",\"name\":\"students\",\"tenantId\":\"tenant1\"}");

		QueryDefinitions definitions = new QueryDefinitions(new Resource[0], catalog().loadDefinitions());
		assertEquals("Tenant.Student.getAll", definitions.retrieve("students", "tenant1").getId());
		assertEquals("Base.Student.getAll", definitions.retrieve("students", "tenant2").getId());
	}

	@Test
	void invalidEnabledConfigurationFailsBeforeServingTraffic() {
		jdbcTemplate.update("insert into chenile_query_definition_source values (?, ?, ?, true)",
				"__base__", "students", "{\"id\":\"Missing.statement\",\"name\":\"different\"}");

		assertThrows(IllegalStateException.class, () -> catalog().loadDefinitions());
	}

	@Test
	void definitionIdIsRequired() {
		jdbcTemplate.update("insert into chenile_query_definition_source values (?, ?, ?, true)",
				"__base__", "students", "{\"name\":\"students\"}");

		IllegalStateException error = assertThrows(IllegalStateException.class, () -> catalog().loadDefinitions());
		org.junit.jupiter.api.Assertions.assertTrue(error.getMessage().contains("definition JSON id"));
	}

	@Test
	void disabledRowsAreIgnoredAndDoNotBlockStartup() {
		jdbcTemplate.update("insert into chenile_query_mapper_source values (?, ?, false)",
				"wrong", "not XML");
		jdbcTemplate.update("insert into chenile_query_definition_source values (?, ?, ?, false)",
				"__base__", "students", "not JSON");

		assertEquals(java.util.List.of(), catalog().loadMappers());
		assertEquals(java.util.List.of(), catalog().loadDefinitions());
	}

	@Test
	void tenantScopeMustMatchDefinitionTenant() {
		jdbcTemplate.update("insert into chenile_query_definition_source values (?, ?, ?, true)",
				"tenant1", "students", "{\"id\":\"Student.getAll\",\"name\":\"students\",\"tenantId\":\"tenant2\"}");

		IllegalStateException error = assertThrows(IllegalStateException.class, () -> catalog().loadDefinitions());
		org.junit.jupiter.api.Assertions.assertTrue(error.getMessage().contains("scope_key"));
	}

	@Test
	void mapperColumnNamespaceMustMatchXmlNamespace() {
		jdbcTemplate.update("insert into chenile_query_mapper_source values (?, ?, true)",
				"Expected.Namespace", mapperXml("Actual.Namespace", "select 1 as id"));

		IllegalStateException error = assertThrows(IllegalStateException.class, () -> catalog().loadMappers());
		org.junit.jupiter.api.Assertions.assertTrue(error.getMessage().contains("does not match"));
	}

	@Test
	void duplicateDatabaseNamespacesAreRejected() {
		assertThrows(IllegalStateException.class, () -> QueryCatalogSnapshot.databasePreferred(
				new Resource[0], new Resource[0], java.util.List.of(
						new JdbcQueryCatalog.MapperSource("Student", mapperXml("Student", "select 1 as id")),
						new JdbcQueryCatalog.MapperSource("Student", mapperXml("Student", "select 2 as id"))),
				java.util.List.of()));
	}

	@Test
	void unavailableCatalogTablesProduceCatalogSpecificStartupError() {
		DataSource emptyDataSource = new DriverManagerDataSource("jdbc:h2:mem:missing_catalog;DB_CLOSE_DELAY=-1", "sa", "");
		JdbcQueryCatalog emptyCatalog = new JdbcQueryCatalog(emptyDataSource, properties, JsonMapper.builder().build());

		IllegalStateException error = assertThrows(IllegalStateException.class, emptyCatalog::loadMappers);
		org.junit.jupiter.api.Assertions.assertTrue(error.getMessage().contains("chenile_query_mapper_source"));
	}

	@Test
	void unresolvedDatabaseStatementFailsValidation() {
		QueryCatalogSnapshot snapshot = QueryCatalogSnapshot.databasePreferred(new Resource[0], new Resource[0],
				java.util.List.of(), java.util.List.of(metadata("Missing.statement", "students")));
		assertThrows(IllegalStateException.class,
				() -> snapshot.validateDatabaseDefinitions(new org.apache.ibatis.session.Configuration()));
	}

	private JdbcQueryCatalog catalog() {
		return new JdbcQueryCatalog(dataSource, properties, JsonMapper.builder().build());
	}

	private static org.chenile.query.model.QueryMetadata metadata(String id, String name) {
		org.chenile.query.model.QueryMetadata metadata = new org.chenile.query.model.QueryMetadata();
		metadata.setId(id);
		metadata.setName(name);
		return metadata;
	}

	private static Resource resource(String content) {
		return new ByteArrayResource(content.getBytes(java.nio.charset.StandardCharsets.UTF_8));
	}

	private static String mapperXml(String namespace, String select) {
		return """
				<?xml version="1.0" encoding="UTF-8" ?>
				<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "https://mybatis.org/dtd/mybatis-3-mapper.dtd">
				<mapper namespace="%s"><select id="getAll" resultType="map">%s</select></mapper>
				""".formatted(namespace, select);
	}
}
