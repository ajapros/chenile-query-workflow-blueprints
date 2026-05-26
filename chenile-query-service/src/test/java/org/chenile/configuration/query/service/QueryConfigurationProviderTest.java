package org.chenile.configuration.query.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.chenile.query.service.impl.MybatisQueryExecutionProvider;
import org.chenile.query.service.impl.QueryExecutionProvider;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;

class QueryConfigurationProviderTest {

	@Test
	void defaultProviderIsMybatis() {
		QueryExecutionProvider mybatisProvider = provider(MybatisQueryExecutionProvider.PROVIDER_NAME);
		QueryExecutionProvider extensionProvider = provider("warehouse");

		QueryExecutionProvider selectedProvider = new QueryConfiguration().queryExecutionProvider(
				new QueryDatasourcesProperties(), List.of(mybatisProvider, extensionProvider));

		assertSame(mybatisProvider, selectedProvider);
	}

	@Test
	void selectsExtensionProviderFromYamlProperty() {
		QueryDatasourcesProperties properties = bind("query.provider", "warehouse");
		QueryExecutionProvider mybatisProvider = provider(MybatisQueryExecutionProvider.PROVIDER_NAME);
		QueryExecutionProvider extensionProvider = provider("warehouse");

		QueryExecutionProvider selectedProvider = new QueryConfiguration().queryExecutionProvider(
				properties, List.of(mybatisProvider, extensionProvider));

		assertEquals("warehouse", properties.getProvider());
		assertSame(extensionProvider, selectedProvider);
	}

	@Test
	void rejectsUnsupportedProvider() {
		QueryDatasourcesProperties properties = bind("query.provider", "unknown");
		QueryExecutionProvider mybatisProvider = provider(MybatisQueryExecutionProvider.PROVIDER_NAME);
		QueryExecutionProvider extensionProvider = provider("warehouse");

		assertThrows(IllegalArgumentException.class, () -> new QueryConfiguration().queryExecutionProvider(
				properties, List.of(mybatisProvider, extensionProvider)));
	}

	private QueryDatasourcesProperties bind(String key, String value) {
		return new Binder(new MapConfigurationPropertySource(Map.of(key, value)))
				.bind("query", QueryDatasourcesProperties.class)
				.get();
	}

	private QueryExecutionProvider provider(String providerName) {
		QueryExecutionProvider provider = mock(QueryExecutionProvider.class);
		when(provider.getProviderName()).thenReturn(providerName);
		return provider;
	}
}
