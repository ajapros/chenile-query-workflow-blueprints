package org.chenile.query.service.impl;

import org.chenile.query.model.QueryMetadata;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QueryDefinitionsTest {

	@Test
	void jsonDefinitionsBindCountQueryOverrideValues() throws IOException {
		QueryDefinitions queryDefinitions = new QueryDefinitions(new ClassPathResource[] {
				new ClassPathResource("org/chenile/samples/query/service/mapper/count-query-overrides.json")
		});

		QueryMetadata forceCount = queryDefinitions.retrieve("students-force-count");
		QueryMetadata noCount = queryDefinitions.retrieve("students-no-count");
		QueryMetadata inheritCount = queryDefinitions.retrieve("students-inherit-count");

		assertTrue(forceCount.getCountQueryEnabled());
		assertFalse(noCount.getCountQueryEnabled());
		assertNull(inheritCount.getCountQueryEnabled());
	}

	@Test
	void tenantSpecificDefinitionOverridesBaseDefinitionForSameQueryName() throws IOException {
		QueryDefinitions queryDefinitions = tenantOverrideDefinitions();

		QueryMetadata base = queryDefinitions.retrieve("tenant-overridden");
		QueryMetadata tenant1 = queryDefinitions.retrieve("tenant-overridden", "tenant1");

		assertEquals("Student.getAll", base.getId());
		assertEquals("tenant1.Student.getAll", tenant1.getId());
		assertEquals("tenant1", tenant1.getTenantId());
	}

	@Test
	void tenantSpecificLookupFallsBackToBaseDefinitionWhenTenantOverrideIsMissing() throws IOException {
		QueryDefinitions queryDefinitions = tenantOverrideDefinitions();

		QueryMetadata tenant3 = queryDefinitions.retrieve("tenant-overridden", "tenant3");

		assertEquals("Student.getAll", tenant3.getId());
		assertNull(tenant3.getTenantId());
	}

	@Test
	void tenantSpecificLookupSupportsTenantOnlyDefinitions() throws IOException {
		QueryDefinitions queryDefinitions = tenantOverrideDefinitions();

		assertNull(queryDefinitions.retrieve("tenant-only"));
		assertEquals("tenant2.Student.getAll", queryDefinitions.retrieve("tenant-only", "tenant2").getId());
	}

	@Test
	void discoveredDefinitionsIncludeBaseAndTenantDefinitions() throws IOException {
		QueryDefinitions queryDefinitions = tenantOverrideDefinitions();

		assertEquals(3, queryDefinitions.getAllDiscoveredQueryDefinitions().size());
	}

	private QueryDefinitions tenantOverrideDefinitions() throws IOException {
		return new QueryDefinitions(new ClassPathResource[] {
				new ClassPathResource("org/chenile/samples/query/service/mapper/tenant-query-overrides.json")
		});
	}
}
