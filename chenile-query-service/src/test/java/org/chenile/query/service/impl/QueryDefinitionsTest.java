package org.chenile.query.service.impl;

import org.chenile.query.model.QueryMetadata;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

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
}
