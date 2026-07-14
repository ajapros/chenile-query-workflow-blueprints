package org.chenile.configuration.query.service;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.function.Function;

import org.chenile.core.context.ContextContainer;
import org.chenile.core.context.ChenileExchange;
import org.chenile.query.service.impl.QueryDefinitions;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

class QueryConfigurationAuthoritiesTest {
	@Test
	void returnsAclsForConfiguredQuery() throws IOException {
		QueryDefinitions queryDefinitions = new QueryDefinitions(
				new ClassPathResource[] {
						new ClassPathResource("org/chenile/samples/query/service/mapper/student.json")
				});
		Function<ChenileExchange, String[]> queryAuthorities = authorities(queryDefinitions, "tenant1");

		ChenileExchange exchange = new ChenileExchange();
		exchange.setHeader("queryName", "students");

		assertArrayEquals(new String[] { "QUERY_READ", "QUERY_EXPORT" }, queryAuthorities.apply(exchange));
	}

	@Test
	void tenantSpecificAclsOverrideBaseAcls() throws IOException {
		QueryDefinitions queryDefinitions = new QueryDefinitions(
				new ClassPathResource[] {
						new ClassPathResource("org/chenile/samples/query/service/mapper/tenant-query-overrides.json")
				});
		Function<ChenileExchange, String[]> tenantAuthorities = authorities(queryDefinitions, "tenant1");
		Function<ChenileExchange, String[]> fallbackAuthorities = authorities(queryDefinitions, "tenant3");

		ChenileExchange exchange = new ChenileExchange();
		exchange.setHeader("queryName", "tenant-overridden");

		assertArrayEquals(new String[] { "TENANT1_QUERY_READ" }, tenantAuthorities.apply(exchange));
		assertArrayEquals(new String[] { "BASE_QUERY_READ" }, fallbackAuthorities.apply(exchange));
	}

	@Test
	void returnsNullWhenQueryNameHeaderIsMissing() throws IOException {
		QueryDefinitions queryDefinitions = new QueryDefinitions(
				new ClassPathResource[] {
						new ClassPathResource("org/chenile/samples/query/service/mapper/student.json")
				});
		Function<ChenileExchange, String[]> queryAuthorities = authorities(queryDefinitions, "tenant1");

		assertNull(queryAuthorities.apply(new ChenileExchange()));
	}

	@Test
	void returnsNullWhenQueryMetadataIsMissing() throws IOException {
		QueryDefinitions queryDefinitions = new QueryDefinitions(
				new ClassPathResource[] {
						new ClassPathResource("org/chenile/samples/query/service/mapper/student.json")
				});
		Function<ChenileExchange, String[]> queryAuthorities = authorities(queryDefinitions, "tenant1");

		ChenileExchange exchange = new ChenileExchange();
		exchange.setHeader("queryName", "unknown-query");

		assertNull(queryAuthorities.apply(exchange));
	}

	private Function<ChenileExchange, String[]> authorities(QueryDefinitions queryDefinitions, String tenant) {
		QueryDatasourcesProperties properties = new QueryDatasourcesProperties();
		properties.setDefaultTenantId("tenant1");
		ContextContainer contextContainer = mock(ContextContainer.class);
		when(contextContainer.getTenant()).thenReturn(tenant);
		return new QueryConfiguration().queryAuthorities(queryDefinitions,
				new QueryTenantResolver(properties, contextContainer));
	}
}
