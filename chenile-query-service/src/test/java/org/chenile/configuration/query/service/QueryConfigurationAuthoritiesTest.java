package org.chenile.configuration.query.service;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.util.function.Function;

import org.chenile.core.context.ChenileExchange;
import org.chenile.query.service.impl.QueryDefinitions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

class QueryConfigurationAuthoritiesTest {
	private Function<ChenileExchange, String[]> queryAuthorities;

	@BeforeEach
	void setUp() throws IOException {
		QueryDefinitions queryDefinitions = new QueryDefinitions(
				new ClassPathResource[] {
						new ClassPathResource("org/chenile/samples/query/service/mapper/student.json")
				});
		queryAuthorities = new QueryConfiguration().queryAuthorities(queryDefinitions);
	}

	@Test
	void returnsAclsForConfiguredQuery() {
		ChenileExchange exchange = new ChenileExchange();
		exchange.setHeader("queryName", "students");

		assertArrayEquals(new String[] { "QUERY_READ", "QUERY_EXPORT" }, queryAuthorities.apply(exchange));
	}

	@Test
	void returnsNullWhenQueryNameHeaderIsMissing() {
		assertNull(queryAuthorities.apply(new ChenileExchange()));
	}

	@Test
	void returnsNullWhenQueryMetadataIsMissing() {
		ChenileExchange exchange = new ChenileExchange();
		exchange.setHeader("queryName", "unknown-query");

		assertNull(queryAuthorities.apply(exchange));
	}
}
