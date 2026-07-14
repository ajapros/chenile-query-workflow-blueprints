package org.chenile.query.service;

import org.chenile.query.model.QueryMetadata;

public interface QueryStore {
	public QueryMetadata retrieve(String queryId);

	default QueryMetadata retrieve(String queryId, String tenantId) {
		return retrieve(queryId);
	}
}
