package org.chenile.query.service.catalog;

import org.chenile.configuration.query.service.QueryCatalogJdbcProperties;
import org.chenile.query.model.QueryMetadata;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.dao.DataAccessException;
import tools.jackson.databind.json.JsonMapper;

import javax.sql.DataSource;
import java.util.List;

/** Reads trusted query configuration from a deployment-managed JDBC catalog. */
public final class JdbcQueryCatalog {
	private final JdbcTemplate jdbcTemplate;
	private final QueryCatalogJdbcProperties properties;
	private final JsonMapper jsonMapper;

	public JdbcQueryCatalog(DataSource dataSource, QueryCatalogJdbcProperties properties, JsonMapper jsonMapper) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
		this.properties = properties;
		this.jsonMapper = jsonMapper;
	}

	public List<MapperSource> loadMappers() {
		try {
			return jdbcTemplate.query("""
					select namespace, mapper_xml
				from chenile_query_mapper_source
				where enabled = true
					order by namespace
				""", (rs, rowNum) -> {
			String namespace = required(rs.getString("namespace"), "namespace", "mapper row " + rowNum);
			String xml = required(rs.getString("mapper_xml"), "mapper_xml", "mapper " + namespace);
			String declaredNamespace = MapperNamespaceReader.namespace(
					new org.springframework.core.io.ByteArrayResource(xml.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
			if (!namespace.equals(declaredNamespace)) {
				throw new IllegalStateException("Database mapper namespace column '" + namespace
						+ "' does not match XML namespace '" + declaredNamespace + "'");
			}
			return new MapperSource(namespace, xml);
			});
		} catch (DataAccessException e) {
			throw new IllegalStateException("Unable to load mapper rows from chenile_query_mapper_source", e);
		}
	}

	public List<QueryMetadata> loadDefinitions() {
		try {
			return jdbcTemplate.query("""
				select scope_key, query_name, definition_json
				from chenile_query_definition_source
				where enabled = true
				order by scope_key, query_name
				""", (rs, rowNum) -> {
			String scope = required(rs.getString("scope_key"), "scope_key", "definition row " + rowNum);
			String queryName = required(rs.getString("query_name"), "query_name", "definition row " + rowNum);
			String json = required(rs.getString("definition_json"), "definition_json", "definition " + queryName);
			try {
				QueryMetadata definition = jsonMapper.readValue(json, QueryMetadata.class);
				if (!queryName.equals(definition.getName())) {
					throw new IllegalStateException("Database query_name '" + queryName
							+ "' does not match definition JSON name '" + definition.getName() + "'");
				}
				required(definition.getId(), "definition JSON id", "definition " + queryName);
				String tenantId = baseScope().equals(scope) ? null : scope;
				if (definition.getTenantId() != null && !definition.getTenantId().isBlank()
						&& !definition.getTenantId().trim().equals(tenantId)) {
					throw new IllegalStateException("Definition '" + queryName + "' has tenantId '"
							+ definition.getTenantId() + "' but scope_key is '" + scope + "'");
				}
				definition.setTenantId(tenantId);
				return definition;
			} catch (Exception e) {
				if (e instanceof IllegalStateException) throw (IllegalStateException) e;
				throw new IllegalStateException("Invalid definition_json for database query '" + queryName + "'", e);
			}
			});
		} catch (DataAccessException e) {
			throw new IllegalStateException("Unable to load definition rows from chenile_query_definition_source", e);
		}
	}

	private static String required(String value, String field, String context) {
		if (value == null || value.isBlank()) {
			throw new IllegalStateException("Database query catalog requires " + field + " for " + context);
		}
		return value.trim();
	}

	private String baseScope() {
		return required(properties.getBaseScope(), "query.catalog.jdbc.baseScope", "query catalog configuration");
	}

	public record MapperSource(String namespace, String xml) { }
}
