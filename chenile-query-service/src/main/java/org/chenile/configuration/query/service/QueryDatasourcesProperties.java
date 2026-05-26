package org.chenile.configuration.query.service;

import java.util.LinkedHashMap;
import java.util.Map;

public class QueryDatasourcesProperties {
	private String provider = "mybatis";
	private Map<String, Map<String, String>> datasources = new LinkedHashMap<>();
	private String defaultTenantId;

	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}

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
