package org.chenile.query.service.catalog;

import org.apache.ibatis.session.Configuration;
import org.chenile.query.model.QueryMetadata;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Immutable query configuration assembled once during application startup. */
public final class QueryCatalogSnapshot {
	private final Resource[] mapperFiles;
	private final Resource[] definitionFiles;
	private final List<QueryMetadata> databaseDefinitions;

	private QueryCatalogSnapshot(Resource[] mapperFiles, Resource[] definitionFiles,
			List<QueryMetadata> databaseDefinitions) {
		this.mapperFiles = mapperFiles;
		this.definitionFiles = definitionFiles;
		this.databaseDefinitions = List.copyOf(databaseDefinitions);
	}

	public static QueryCatalogSnapshot classpathOnly(Resource[] mapperFiles, Resource[] definitionFiles) {
		return new QueryCatalogSnapshot(mapperFiles, definitionFiles, List.of());
	}

	/**
	 * A database mapper replaces a classpath mapper only when both use the same
	 * MyBatis namespace. This avoids duplicate statement registration while
	 * leaving unrelated packaged mappers available during migration.
	 */
	public static QueryCatalogSnapshot databasePreferred(Resource[] classpathMappers, Resource[] definitionFiles,
			Collection<JdbcQueryCatalog.MapperSource> databaseMappers,
			Collection<QueryMetadata> databaseDefinitions) {
		Set<String> databaseNamespaces = new LinkedHashSet<>();
		List<Resource> mergedMappers = new ArrayList<>();
		for (JdbcQueryCatalog.MapperSource mapper : databaseMappers) {
			if (!databaseNamespaces.add(mapper.namespace())) {
				throw new IllegalStateException("Duplicate enabled database mapper namespace: " + mapper.namespace());
			}
		}
		for (Resource mapper : classpathMappers) {
			String namespace = MapperNamespaceReader.namespace(mapper);
			if (!databaseNamespaces.contains(namespace)) {
				mergedMappers.add(mapper);
			}
		}
		for (JdbcQueryCatalog.MapperSource mapper : databaseMappers) {
			mergedMappers.add(new ByteArrayResource(mapper.xml().getBytes(StandardCharsets.UTF_8),
					"database mapper " + mapper.namespace()));
		}
		return new QueryCatalogSnapshot(mergedMappers.toArray(Resource[]::new), definitionFiles,
				new ArrayList<>(databaseDefinitions));
	}

	public Resource[] mapperFiles() { return mapperFiles; }
	public Resource[] definitionFiles() { return definitionFiles; }
	public List<QueryMetadata> databaseDefinitions() { return databaseDefinitions; }

	/** Validates database metadata against the fully parsed MyBatis configuration. */
	public void validateDatabaseDefinitions(Configuration configuration) {
		for (QueryMetadata definition : databaseDefinitions) {
			if (!configuration.hasStatement(definition.getId(), false)) {
				throw new IllegalStateException("Database query definition '" + definition.getName()
						+ "' references missing MyBatis statement '" + definition.getId() + "'");
			}
		}
	}
}
