package org.chenile.configuration.query.service;

import org.chenile.base.exception.BadRequestException;
import org.chenile.core.context.ContextContainer;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class QueryConfigurationDataSourceRoutingTest {

	private final QueryConfiguration configuration = new QueryConfiguration();

	@Test
	void missingTenantWithoutDefaultTenantFails() throws Exception {
		ContextContainer contextContainer = mock(ContextContainer.class);
		when(contextContainer.getTenant()).thenReturn(null);
		QueryDatasourcesProperties properties = properties(null);

		DataSource dataSource = routingDataSource(properties, contextContainer);

		assertThrows(BadRequestException.class, dataSource::getConnection);
	}

	@Test
	void blankTenantWithoutDefaultTenantFails() throws Exception {
		ContextContainer contextContainer = mock(ContextContainer.class);
		when(contextContainer.getTenant()).thenReturn("   ");
		QueryDatasourcesProperties properties = properties(null);

		DataSource dataSource = routingDataSource(properties, contextContainer);

		assertThrows(BadRequestException.class, dataSource::getConnection);
	}

	@Test
	void missingTenantUsesConfiguredDefaultTenant() throws Exception {
		ContextContainer contextContainer = mock(ContextContainer.class);
		when(contextContainer.getTenant()).thenReturn(null);
		DataSource tenant1 = mockDataSource();
		Map<String, DataSource> targetDataSources = targetDataSources(tenant1, mockDataSource());
		QueryDatasourcesProperties properties = properties("tenant1");

		DataSource dataSource = routingDataSource(targetDataSources, properties, contextContainer);

		assertNotNull(dataSource.getConnection());
		verify(tenant1).getConnection();
	}

	@Test
	void blankTenantUsesConfiguredDefaultTenant() throws Exception {
		ContextContainer contextContainer = mock(ContextContainer.class);
		when(contextContainer.getTenant()).thenReturn("   ");
		DataSource tenant1 = mockDataSource();
		Map<String, DataSource> targetDataSources = targetDataSources(tenant1, mockDataSource());
		QueryDatasourcesProperties properties = properties(" tenant1 ");

		DataSource dataSource = routingDataSource(targetDataSources, properties, contextContainer);

		assertNotNull(dataSource.getConnection());
		verify(tenant1).getConnection();
	}

	@Test
	void configuredTenantRoutesToMatchingDatasource() throws Exception {
		ContextContainer contextContainer = mock(ContextContainer.class);
		when(contextContainer.getTenant()).thenReturn("tenant2");
		DataSource tenant2 = mockDataSource();
		Map<String, DataSource> targetDataSources = targetDataSources(mockDataSource(), tenant2);
		QueryDatasourcesProperties properties = properties("tenant1");

		DataSource dataSource = routingDataSource(targetDataSources, properties, contextContainer);

		assertNotNull(dataSource.getConnection());
		verify(tenant2).getConnection();
	}

	@Test
	void unknownTenantDoesNotFallBackToDefaultTenant() throws Exception {
		ContextContainer contextContainer = mock(ContextContainer.class);
		when(contextContainer.getTenant()).thenReturn("missing");
		QueryDatasourcesProperties properties = properties("tenant1");

		DataSource dataSource = routingDataSource(properties, contextContainer);

		assertThrows(IllegalStateException.class, dataSource::getConnection);
	}

	@Test
	void invalidDefaultTenantFailsAtConfigurationTime() {
		ContextContainer contextContainer = mock(ContextContainer.class);
		QueryDatasourcesProperties properties = properties("missing");

		assertThrows(IllegalStateException.class,
				() -> configuration.queryDataSource(targetDataSources(), properties,
						new QueryTenantResolver(properties, contextContainer)));
	}

	@Test
	void emptyDatasourcesFailAtConfigurationTime() {
		ContextContainer contextContainer = mock(ContextContainer.class);
		QueryDatasourcesProperties properties = properties(null);

		assertThrows(IllegalStateException.class,
				() -> configuration.queryDataSource(Map.of(), properties,
						new QueryTenantResolver(properties, contextContainer)));
	}

	private DataSource routingDataSource(QueryDatasourcesProperties properties, ContextContainer contextContainer)
			throws Exception {
		return routingDataSource(targetDataSources(), properties, contextContainer);
	}

	private DataSource routingDataSource(Map<String, DataSource> targetDataSources,
			QueryDatasourcesProperties properties, ContextContainer contextContainer) {
		DataSource dataSource = configuration.queryDataSource(targetDataSources, properties,
				new QueryTenantResolver(properties, contextContainer));
		((AbstractRoutingDataSource) dataSource).afterPropertiesSet();
		return dataSource;
	}

	private QueryDatasourcesProperties properties(String defaultTenantId) {
		QueryDatasourcesProperties properties = new QueryDatasourcesProperties();
		properties.setDefaultTenantId(defaultTenantId);
		return properties;
	}

	private Map<String, DataSource> targetDataSources() throws Exception {
		return targetDataSources(mockDataSource(), mockDataSource());
	}

	private Map<String, DataSource> targetDataSources(DataSource tenant1, DataSource tenant2) {
		Map<String, DataSource> dataSources = new LinkedHashMap<>();
		dataSources.put("tenant1", tenant1);
		dataSources.put("tenant2", tenant2);
		return dataSources;
	}

	private DataSource mockDataSource() throws Exception {
		DataSource dataSource = mock(DataSource.class);
		when(dataSource.getConnection()).thenReturn(mock(Connection.class));
		return dataSource;
	}
}
