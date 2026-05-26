package org.chenile.configuration.query.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.chenile.core.context.ContextContainer;
import org.chenile.query.model.QueryMetadata;
import org.chenile.query.model.SearchRequest;
import org.chenile.query.model.SearchResponse;
import org.chenile.query.model.SortCriterion;
import org.chenile.query.service.SearchService;
import org.chenile.query.service.impl.QueryExecutionProvider;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootTest(classes = {
		QueryConfiguration.class,
		QueryExecutionProviderExtensionTest.ExtensionProviderConfig.class
}, properties = {
		"query.provider=extension-test",
		"query.mybatis.enabled=false",
		"query.pagination.countQueryEnabled=false",
		"query.definitionFiles=classpath*:org/chenile/samples/query/service/mapper/student.json"
})
class QueryExecutionProviderExtensionTest {
	@Autowired
	SearchService<Map<String, Object>> searchService;
	@Autowired
	ExtensionQueryExecutionProvider extensionProvider;
	@Autowired
	ApplicationContext applicationContext;

	@Test
	void customProviderCanReplaceMybatisQueryExecution() {
		SearchRequest<Map<String, Object>> request = new SearchRequest<>();
		request.setQueryName("students");
		request.setPageNum(2);
		request.setNumRowsInPage(2);
		request.setFilters(Map.of("branch", List.of("Bangalore")));

		SortCriterion sortCriterion = new SortCriterion();
		sortCriterion.setName("branch");
		sortCriterion.setAscendingOrder(true);
		request.setSortCriteria(List.of(sortCriterion));

		SearchResponse response = searchService.search(request);

		assertEquals("Student.getAll", extensionProvider.executedQueryName);
		assertEquals(0, extensionProvider.countCalls);
		assertEquals(1, extensionProvider.queryCalls);
		assertEquals("extension-sort-1", extensionProvider.lastFilters.get("sort"));
		assertEquals(3, extensionProvider.lastFilters.get("sliceSize"));
		assertEquals(3, extensionProvider.lastFilters.get("sliceStart"));
		assertEquals(2, response.getNumRowsReturned());
		assertEquals("row-1", response.getList().get(0).getRow());
		assertNotNull(response.getPagination());
		assertFalse(response.getPagination().getCountQueryExecuted());
		assertFalse(response.getPagination().getTotalCountAvailable());
		assertTrue(response.getPagination().getNextPageAvailable());
		assertEquals(0, applicationContext.getBeanNamesForType(SqlSessionTemplate.class).length);
	}

	@Configuration
	@EnableConfigurationProperties
	static class ExtensionProviderConfig {
		@Bean
		ContextContainer contextContainer() {
			return mock(ContextContainer.class);
		}

		@Bean
		ExtensionQueryExecutionProvider extensionQueryExecutionProvider() {
			return new ExtensionQueryExecutionProvider();
		}
	}

	static class ExtensionQueryExecutionProvider implements QueryExecutionProvider {
		String executedQueryName;
		Map<String, Object> lastFilters = Map.of();
		int countCalls;
		int queryCalls;

		@Override
		public String getProviderName() {
			return "extension-test";
		}

		@Override
		public void applySort(Map<String, Object> filters, List<SortCriterion> sortCriteria, QueryMetadata queryMetadata) {
			filters.put("sort", "extension-sort-" + sortCriteria.size());
		}

		@Override
		public void applyPagination(Map<String, Object> filters, int startRow, int numRowsInPage) {
			filters.put("sliceStart", startRow);
			filters.put("sliceSize", numRowsInPage);
		}

		@Override
		public Object executeCount(String queryName, Map<String, Object> filters) {
			countCalls++;
			return 3;
		}

		@Override
		public List<Object> executeQuery(String queryName, Map<String, Object> filters) {
			queryCalls++;
			executedQueryName = queryName;
			lastFilters = new HashMap<>(filters);
			List<Object> rows = new ArrayList<>();
			rows.add("row-1");
			rows.add("row-2");
			rows.add("row-3");
			return rows;
		}
	}
}
