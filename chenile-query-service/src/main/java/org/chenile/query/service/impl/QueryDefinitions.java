package org.chenile.query.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.chenile.query.model.QueryMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

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
	private final ObjectMapper objectMapper = new ObjectMapper();
	private final Map<String, Map<String, QueryMetadata>> tenantStore = new HashMap<>();
	public QueryDefinitions(Resource[] queryDefinitionFiles) throws IOException {
		for (Resource file: queryDefinitionFiles ) {
			processFile(file);
		}
	}
	private void processFile(Resource file) throws IOException {
		String content = file.getContentAsString(Charset.defaultCharset());			
		List<QueryMetadata> queries  = objectMapper.readValue(content, new TypeReference<List<QueryMetadata>>() {} );
		for (QueryMetadata qm: queries) {
			String tenantId = normalize(qm.getTenantId());
			if (tenantId == null) {
				store.put(qm.getName(), qm);
				logger.debug("Discovered name:" + qm.getName());
			} else {
				tenantStore.computeIfAbsent(tenantId, key -> new HashMap<>()).put(qm.getName(), qm);
				logger.debug("Discovered tenant:" + tenantId + " name:" + qm.getName());
			}
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
