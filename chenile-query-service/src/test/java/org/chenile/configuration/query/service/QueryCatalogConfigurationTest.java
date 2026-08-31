package org.chenile.configuration.query.service;

import org.chenile.core.context.ContextContainer;
import org.chenile.query.service.impl.QueryDefinitions;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

@SpringBootTest(classes = { QueryConfiguration.class, QueryCatalogConfigurationTest.CatalogConfig.class }, properties = {
		"query.defaultTenantId=tenant1",
		"query.datasources.tenant1.jdbcUrl=jdbc:h2:mem:query_catalog_query;DB_CLOSE_DELAY=-1",
		"query.datasources.tenant1.username=sa",
		"query.datasources.tenant1.password=",
		"query.catalog.jdbc.enabled=true",
		"query.catalog.jdbc.data-source=queryCatalogDataSource"
})
class QueryCatalogConfigurationTest {
	@Autowired QueryDefinitions queryDefinitions;
	@Autowired SqlSessionTemplate sqlSessionTemplate;

	@Test
	void enabledCatalogOverridesClasspathDuringSpringStartup() {
		assertEquals("Student.getAll", queryDefinitions.retrieve("students").getId());
		assertTrue(sqlSessionTemplate.getConfiguration().hasStatement("Student.getAll"));
		assertEquals("select 1 as id", sqlSessionTemplate.getConfiguration()
				.getMappedStatement("Student.getAll").getBoundSql(java.util.Map.of()).getSql().trim());
	}

	@Test
	void enabledCatalogRequiresTheConfiguredDatasourceBean() {
		QueryCatalogJdbcProperties properties = new QueryCatalogJdbcProperties();
		properties.setEnabled(true);
		properties.setDataSource("missingCatalogDataSource");

		IllegalStateException error = assertThrows(IllegalStateException.class, () ->
				new QueryConfiguration().queryCatalogSnapshot(properties, new StaticApplicationContext(),
						tools.jackson.databind.json.JsonMapper.builder().build()));
		assertTrue(error.getMessage().contains("missingCatalogDataSource"));
	}

	@Configuration
	@EnableConfigurationProperties
	static class CatalogConfig {
		@Bean
		ContextContainer contextContainer() {
			return mock(ContextContainer.class);
		}

		@Bean("queryCatalogDataSource")
		DataSource queryCatalogDataSource() {
			DriverManagerDataSource dataSource = new DriverManagerDataSource(
					"jdbc:h2:mem:query_catalog_startup;DB_CLOSE_DELAY=-1", "sa", "");
			JdbcTemplate jdbc = new JdbcTemplate(dataSource);
			jdbc.execute("create table chenile_query_mapper_source (namespace varchar(512), mapper_xml clob, enabled boolean)");
			jdbc.execute("create table chenile_query_definition_source (scope_key varchar(255), query_name varchar(255), definition_json clob, enabled boolean)");
			jdbc.update("insert into chenile_query_mapper_source values (?, ?, true)", "Student", mapperXml());
			jdbc.update("insert into chenile_query_definition_source values (?, ?, ?, true)", "__base__", "students",
					"{\"id\":\"Student.getAll\",\"name\":\"students\"}");
			return dataSource;
		}

		private static String mapperXml() {
			return """
					<?xml version="1.0" encoding="UTF-8" ?>
					<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "https://mybatis.org/dtd/mybatis-3-mapper.dtd">
					<mapper namespace="Student"><select id="getAll" resultType="map">select 1 as id</select></mapper>
					""";
		}
	}
}
