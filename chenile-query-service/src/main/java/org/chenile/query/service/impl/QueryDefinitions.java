package org.chenile.query.service.impl;

import org.chenile.query.model.QueryMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Reads the query definitions from a JSON file that has been passed to it.
 * There can be multiple JSON files with the same name in the project. It reads them all
 * and gathers all the definitions in one place. 
 */
public class QueryDefinitions extends BaseQueryStore{
	private final Logger logger = LoggerFactory.getLogger(QueryDefinitions.class);
	private final JsonMapper jsonMapper;
	private final Map<String, Map<String, QueryMetadata>> tenantStore = new HashMap<>();
	public QueryDefinitions(Resource[] queryDefinitionFiles) throws IOException {
		this(queryDefinitionFiles, List.of(), JsonMapper.builder().build());
	}

	/**
	 * Database definitions are applied after classpath definitions so a matching
	 * base or tenant-specific name is an intentional startup-time override.
	 */
	public QueryDefinitions(Resource[] queryDefinitionFiles, List<QueryMetadata> databaseDefinitions) throws IOException {
		this(queryDefinitionFiles, databaseDefinitions, JsonMapper.builder().build());
	}

	public QueryDefinitions(Resource[] queryDefinitionFiles, List<QueryMetadata> databaseDefinitions,
			JsonMapper jsonMapper) throws IOException {
		this.jsonMapper = jsonMapper;
		for (Resource file: queryDefinitionFiles ) {
			processFile(file);
		}
		for (QueryMetadata definition : databaseDefinitions) {
			addOrReplace(definition);
		}
	}
	private void processFile(Resource file) throws IOException {
		String content = file.getContentAsString(Charset.defaultCharset());			
		List<QueryMetadata> queries  = jsonMapper.readValue(content, new TypeReference<List<QueryMetadata>>() {} );
		for (QueryMetadata qm: queries) {
			addOrReplace(qm);
		}
	}

	public void addOrReplace(QueryMetadata queryMetadata) {
		if (queryMetadata == null || normalize(queryMetadata.getName()) == null) {
			throw new IllegalArgumentException("Query definition name is required");
		}
		String tenantId = normalize(queryMetadata.getTenantId());
		if (tenantId == null) {
			store.put(queryMetadata.getName(), queryMetadata);
			logger.debug("Discovered name:" + queryMetadata.getName());
		} else {
			tenantStore.computeIfAbsent(tenantId, key -> new HashMap<>()).put(queryMetadata.getName(), queryMetadata);
			logger.debug("Discovered tenant:" + tenantId + " name:" + queryMetadata.getName());
		}
	}

	public List<QueryMetadata> getAllDiscoveredQueryDefinitions() {
		List<QueryMetadata> allDefinitions = new ArrayList<>(store.values());
		for (Map<String, QueryMetadata> tenantQueries : tenantStore.values()) {
			allDefinitions.addAll(tenantQueries.values());
		}
		return List.copyOf(allDefinitions);
	}

	@Override
	public QueryMetadata retrieveQueryIdFromStore(String queryId) {
		return store.get(queryId);
	}

	@Override
	public QueryMetadata retrieve(String queryId) {
		return store.get(queryId);
	}

	@Override
	public QueryMetadata retrieve(String queryId, String tenantId) {
		String normalizedTenantId = normalize(tenantId);
		if (normalizedTenantId != null) {
			Map<String, QueryMetadata> tenantQueries = tenantStore.get(normalizedTenantId);
			if (tenantQueries != null && tenantQueries.containsKey(queryId)) {
				return tenantQueries.get(queryId);
			}
		}
		return retrieve(queryId);
	}

	private String normalize(String value) {
		if (value == null) {
			return null;
		}
		String normalized = value.trim();
		return normalized.isEmpty() ? null : normalized;
	}
}
