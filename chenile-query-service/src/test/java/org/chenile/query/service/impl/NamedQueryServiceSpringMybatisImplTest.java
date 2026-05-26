package org.chenile.query.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.chenile.configuration.query.service.QueryPaginationProperties;
import org.chenile.query.model.QueryMetadata;
import org.chenile.query.model.SearchRequest;
import org.chenile.query.model.SearchResponse;
import org.chenile.query.service.QueryStore;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mybatis.spring.SqlSessionTemplate;

class NamedQueryServiceSpringMybatisImplTest {

	@Test
	void paginatedSearchRunsCountQueryByDefault() {
		QueryStore queryStore = mock(QueryStore.class);
		when(queryStore.retrieve("students")).thenReturn(queryMetadata(true));
		SqlSessionTemplate sessionTemplate = mock(SqlSessionTemplate.class);
		when(sessionTemplate.selectOne(eq("Student.getAll-count"), anyMap())).thenReturn(25);
		when(sessionTemplate.selectList(eq("Student.getAll"), anyMap())).thenReturn(rows(10));

		NamedQueryServiceSpringMybatisImpl service = service(queryStore, sessionTemplate, true);
		SearchResponse response = service.search(searchRequest(2, 10));

		verify(sessionTemplate).selectOne(eq("Student.getAll-count"), anyMap());
		assertEquals(10, response.getNumRowsReturned());
		assertEquals(2, response.getCurrentPage());
		assertEquals(25, response.getMaxRows());
		assertEquals(4, response.getMaxPages());
		assertNull(response.getPagination());
		assertEquals("limit 10 offset 10", capturedFilters(sessionTemplate).get("pagination"));
	}

	@Test
	void countQueryAcceptsLongCountsFromJdbcDrivers() {
		QueryStore queryStore = mock(QueryStore.class);
		when(queryStore.retrieve("students")).thenReturn(queryMetadata(true));
		SqlSessionTemplate sessionTemplate = mock(SqlSessionTemplate.class);
		when(sessionTemplate.selectOne(eq("Student.getAll-count"), anyMap())).thenReturn(25L);
		when(sessionTemplate.selectList(eq("Student.getAll"), anyMap())).thenReturn(rows(10));

		NamedQueryServiceSpringMybatisImpl service = service(queryStore, sessionTemplate, true);
		SearchResponse response = service.search(searchRequest(2, 10));

		assertEquals(25, response.getMaxRows());
		assertEquals(4, response.getMaxPages());
	}

	@Test
	void noCountPaginationFetchesOneExtraRowAndTrimsIt() {
		QueryStore queryStore = mock(QueryStore.class);
		when(queryStore.retrieve("students")).thenReturn(queryMetadata(true));
		SqlSessionTemplate sessionTemplate = mock(SqlSessionTemplate.class);
		when(sessionTemplate.selectList(eq("Student.getAll"), anyMap())).thenReturn(rows(3));

		NamedQueryServiceSpringMybatisImpl service = service(queryStore, sessionTemplate, false);
		SearchResponse response = service.search(searchRequest(3, 2));

		verify(sessionTemplate, never()).selectOne(eq("Student.getAll-count"), anyMap());
		assertEquals(2, response.getNumRowsReturned());
		assertEquals(2, response.getList().size());
		assertEquals("row-1", response.getList().get(0).getRow());
		assertEquals("row-2", response.getList().get(1).getRow());
		assertEquals(3, response.getCurrentPage());
		assertEquals(5, response.getStartRow());
		assertEquals(0, response.getMaxRows());
		assertEquals(0, response.getMaxPages());
		assertNotNull(response.getPagination());
		assertFalse(response.getPagination().getCountQueryExecuted());
		assertFalse(response.getPagination().getTotalCountAvailable());
		assertTrue(response.getPagination().getNextPageAvailable());
		assertEquals("limit 3 offset 4", capturedFilters(sessionTemplate).get("pagination"));
	}

	@Test
	void noCountPaginationMarksLastPageWhenExtraRowIsMissing() {
		QueryStore queryStore = mock(QueryStore.class);
		when(queryStore.retrieve("students")).thenReturn(queryMetadata(true));
		SqlSessionTemplate sessionTemplate = mock(SqlSessionTemplate.class);
		when(sessionTemplate.selectList(eq("Student.getAll"), anyMap())).thenReturn(rows(2));

		NamedQueryServiceSpringMybatisImpl service = service(queryStore, sessionTemplate, false);
		SearchResponse response = service.search(searchRequest(1, 2));

		verify(sessionTemplate, never()).selectOne(eq("Student.getAll-count"), anyMap());
		assertEquals(2, response.getNumRowsReturned());
		assertFalse(response.getPagination().getNextPageAvailable());
		assertEquals("limit 3 offset 0", capturedFilters(sessionTemplate).get("pagination"));
	}

	@Test
	void noCountPaginationHandlesEmptyOutOfRangePageWithoutClamping() {
		QueryStore queryStore = mock(QueryStore.class);
		when(queryStore.retrieve("students")).thenReturn(queryMetadata(true));
		SqlSessionTemplate sessionTemplate = mock(SqlSessionTemplate.class);
		when(sessionTemplate.selectList(eq("Student.getAll"), anyMap())).thenReturn(Collections.emptyList());

		NamedQueryServiceSpringMybatisImpl service = service(queryStore, sessionTemplate, false);
		SearchResponse response = service.search(searchRequest(99, 20));

		verify(sessionTemplate, never()).selectOne(eq("Student.getAll-count"), anyMap());
		assertEquals(0, response.getNumRowsReturned());
		assertEquals(99, response.getCurrentPage());
		assertEquals(1961, response.getStartRow());
		assertFalse(response.getPagination().getNextPageAvailable());
		assertEquals("limit 21 offset 1960", capturedFilters(sessionTemplate).get("pagination"));
	}

	@Test
	void customProviderCanOverridePaginationRenderingWhenCountIsDisabled() {
		QueryStore queryStore = mock(QueryStore.class);
		when(queryStore.retrieve("students")).thenReturn(queryMetadata(true));
		SqlSessionTemplate sessionTemplate = mock(SqlSessionTemplate.class);
		when(sessionTemplate.selectList(eq("Student.getAll"), anyMap())).thenReturn(rows(3));

		NamedQueryServiceSpringMybatisImpl service = service(queryStore, sessionTemplate, false);
		service.setQueryExecutionProvider(new TestQueryExecutionProvider(sessionTemplate));
		SearchResponse response = service.search(searchRequest(3, 2));

		verify(sessionTemplate, never()).selectOne(eq("Student.getAll-count"), anyMap());
		assertEquals(2, response.getNumRowsReturned());
		assertTrue(response.getPagination().getNextPageAvailable());
		assertEquals("fetch 3 skip 4", capturedFilters(sessionTemplate).get("pagination"));
	}

	@Test
	void nonPaginatedSearchDoesNotAttachPaginationMetadata() {
		QueryStore queryStore = mock(QueryStore.class);
		when(queryStore.retrieve("students")).thenReturn(queryMetadata(false));
		SqlSessionTemplate sessionTemplate = mock(SqlSessionTemplate.class);
		when(sessionTemplate.selectList(eq("Student.getAll"), anyMap())).thenReturn(rows(2));

		NamedQueryServiceSpringMybatisImpl service = service(queryStore, sessionTemplate, false);
		SearchResponse response = service.search(searchRequest(1, 2));

		verify(sessionTemplate, never()).selectOne(eq("Student.getAll-count"), anyMap());
		assertEquals(2, response.getNumRowsReturned());
		assertNull(response.getPagination());
		assertFalse(capturedFilters(sessionTemplate).containsKey("pagination"));
	}

	private NamedQueryServiceSpringMybatisImpl service(QueryStore queryStore, SqlSessionTemplate sessionTemplate,
			boolean countQueryEnabled) {
		QueryPaginationProperties paginationProperties = new QueryPaginationProperties();
		paginationProperties.setCountQueryEnabled(countQueryEnabled);
		NamedQueryServiceSpringMybatisImpl service = new NamedQueryServiceSpringMybatisImpl(queryStore);
		service.setSessionTemplate(sessionTemplate);
		service.setPaginationProperties(paginationProperties);
		return service;
	}

	private QueryMetadata queryMetadata(boolean paginated) {
		QueryMetadata queryMetadata = new QueryMetadata();
		queryMetadata.setName("students");
		queryMetadata.setId("Student.getAll");
		queryMetadata.setColumnMetadata(Collections.emptyMap());
		queryMetadata.setPaginated(paginated);
		return queryMetadata;
	}

	private SearchRequest<Map<String, Object>> searchRequest(int pageNum, int pageSize) {
		SearchRequest<Map<String, Object>> searchRequest = new SearchRequest<>();
		searchRequest.setQueryName("students");
		searchRequest.setFilters(Collections.emptyMap());
		searchRequest.setPageNum(pageNum);
		searchRequest.setNumRowsInPage(pageSize);
		return searchRequest;
	}

	private List<Object> rows(int count) {
		List<Object> rows = new ArrayList<>();
		for (int i = 1; i <= count; i++) {
			rows.add("row-" + i);
		}
		return rows;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Map<String, Object> capturedFilters(SqlSessionTemplate sessionTemplate) {
		ArgumentCaptor<Map> filtersCaptor = ArgumentCaptor.forClass(Map.class);
		verify(sessionTemplate).selectList(eq("Student.getAll"), filtersCaptor.capture());
		return filtersCaptor.getValue();
	}

	private static class TestQueryExecutionProvider extends MybatisQueryExecutionProvider {
		TestQueryExecutionProvider(SqlSessionTemplate sessionTemplate) {
			super(sessionTemplate);
		}

		@Override
		public String getProviderName() {
			return "test";
		}

		@Override
		public void applyPagination(Map<String, Object> filters, int startRow, int numRowsInPage) {
			filters.put(PAGINATION_PART, "fetch " + numRowsInPage + " skip " + (startRow - 1));
		}
	}
}
