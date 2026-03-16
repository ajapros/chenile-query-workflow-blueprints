package org.chenile.query.service.interceptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.chenile.core.context.ChenileExchange;
import org.chenile.core.context.ContextContainer;
import org.chenile.query.model.ResponseRow;
import org.chenile.query.model.SearchRequest;
import org.chenile.query.model.SearchResponse;
import org.chenile.query.service.SearchService;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class QueryUserFilterInterceptorTest {

	@Test
	@SuppressWarnings("unchecked")
	void addsAuthIdsToSystemFiltersForRegularQueries() {
		ContextContainer contextContainer = mock(ContextContainer.class);
		when(contextContainer.getAuthUser()).thenReturn("manager1");
		SearchService<Map<String, Object>> searchService = mock(SearchService.class);

		SearchResponse searchResponse = new SearchResponse();
		ResponseRow first = new ResponseRow();
		first.setRow(new HashMap<>(Map.of("authId", "rep1")));
		ResponseRow second = new ResponseRow();
		second.setRow(new HashMap<>(Map.of("authId", "rep2")));
		searchResponse.setList(new ArrayList<>());
		searchResponse.getList().add(first);
		searchResponse.getList().add(second);
		when(searchService.search(org.mockito.ArgumentMatchers.any(SearchRequest.class))).thenReturn(searchResponse);

		TestableQueryUserFilterInterceptor interceptor = new TestableQueryUserFilterInterceptor();
		ReflectionTestUtils.setField(interceptor, "contextContainer", contextContainer);
		ReflectionTestUtils.setField(interceptor, "searchService", searchService);
		ReflectionTestUtils.setField(interceptor, "skipAuthIds", "");

		SearchRequest<Map<String, Object>> searchRequest = new SearchRequest<>();
		searchRequest.setQueryName("students");
		ChenileExchange exchange = new ChenileExchange();
		exchange.setBody(searchRequest);

		interceptor.invokePreProcessing(exchange);

		assertEquals(
				Arrays.asList("rep1", "rep2", "manager1"),
				searchRequest.getSystemFilters().get("authIds"));
		verify(searchService).search(org.mockito.ArgumentMatchers.any(SearchRequest.class));
	}

	@Test
	void skipsAuthIdEnhancementForConfiguredQueries() {
		ContextContainer contextContainer = mock(ContextContainer.class);
		when(contextContainer.getAuthUser()).thenReturn("manager1");

		TestableQueryUserFilterInterceptor interceptor = new TestableQueryUserFilterInterceptor();
		ReflectionTestUtils.setField(interceptor, "contextContainer", contextContainer);
		ReflectionTestUtils.setField(interceptor, "skipAuthIds", "students,otherQuery");

		SearchRequest<Map<String, Object>> searchRequest = new SearchRequest<>();
		searchRequest.setQueryName("students");
		ChenileExchange exchange = new ChenileExchange();
		exchange.setBody(searchRequest);

		interceptor.invokePreProcessing(exchange);

		assertNull(searchRequest.getSystemFilters());
	}

	private static class TestableQueryUserFilterInterceptor extends QueryUserFilterInterceptor {
		private void invokePreProcessing(ChenileExchange exchange) {
			doPreProcessing(exchange);
		}
	}
}
