package org.chenile.query.service.impl;

import java.util.List;
import java.util.Map;

import org.chenile.query.model.QueryMetadata;
import org.chenile.query.model.SortCriterion;

public interface QueryExecutionProvider {
	String ORDER_BY_PART = "orderby";
	String PAGINATION_PART = "pagination";

	String getProviderName();

	void applySort(Map<String, Object> filters, List<SortCriterion> sortCriteria, QueryMetadata queryMetadata);

	void applyPagination(Map<String, Object> filters, int startRow, int numRowsInPage);

	Object executeCount(String queryName, Map<String, Object> filters);

	List<Object> executeQuery(String queryName, Map<String, Object> filters);
}
