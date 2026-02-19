package org.chenile.configuration.query.service;

import java.util.LinkedHashMap;
import java.util.Map;

public class QueryDatasourcesProperties {
	private Map<String, Map<String, String>> datasources = new LinkedHashMap<>();
	private String defaultTenantId;

	public Map<String, Map<String, String>> getDatasources() {
		return datasources;
	}

	public void setDatasources(Map<String, Map<String, String>> datasources) {
		this.datasources = datasources;
	}

	public String getDefaultTenantId() {
		return defaultTenantId;
	}

	public void setDefaultTenantId(String defaultTenantId) {
		this.defaultTenantId = defaultTenantId;
	}
}
