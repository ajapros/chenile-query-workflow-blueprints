package org.chenile.configuration.query.service;

import org.chenile.base.exception.BadRequestException;
import org.chenile.core.context.ContextContainer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class QueryTenantResolverTest {

	@Test
	void explicitTenantTakesPrecedenceOverDefaultTenant() {
		QueryTenantResolver resolver = resolver(" tenant2 ", "tenant1");

		assertEquals("tenant2", resolver.resolveTenant());
	}

	@Test
	void missingTenantUsesConfiguredDefaultTenant() {
		QueryTenantResolver resolver = resolver(null, " tenant1 ");

		assertEquals("tenant1", resolver.resolveTenant());
	}

	@Test
	void blankTenantUsesConfiguredDefaultTenant() {
		QueryTenantResolver resolver = resolver("   ", "tenant1");

		assertEquals("tenant1", resolver.resolveTenant());
	}

	@Test
	void missingTenantWithoutDefaultTenantFails() {
		QueryTenantResolver resolver = resolver(null, null);

		assertThrows(BadRequestException.class, resolver::resolveTenant);
	}

	@Test
	void blankTenantWithoutDefaultTenantFails() {
		QueryTenantResolver resolver = resolver("   ", "   ");

		assertThrows(BadRequestException.class, resolver::resolveTenant);
	}

	@Test
	void defaultTenantIdIsNormalized() {
		QueryTenantResolver resolver = resolver(null, " tenant1 ");

		assertEquals("tenant1", resolver.getDefaultTenantId());
	}

	private QueryTenantResolver resolver(String currentTenant, String defaultTenant) {
		QueryDatasourcesProperties properties = new QueryDatasourcesProperties();
		properties.setDefaultTenantId(defaultTenant);
		ContextContainer contextContainer = mock(ContextContainer.class);
		when(contextContainer.getTenant()).thenReturn(currentTenant);
		return new QueryTenantResolver(properties, contextContainer);
	}
}
