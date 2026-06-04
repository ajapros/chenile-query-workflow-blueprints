package org.chenile.workflow.service.stmcmds;

import org.chenile.core.model.OperationDefinition;
import org.chenile.core.model.ParamDefinition;
import org.chenile.mcp.model.ChenilePolymorphVariant;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import tools.jackson.core.type.TypeReference;

import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

class ProcessIdPolymorphTest {

	@Test
	void createsOneVariantPerWorkflowEvent() {
		TypeReference<?> assignPayload = typeReference(AssignPayload.class);
		TypeReference<?> resolvePayload = typeReference(ResolvePayload.class);
		StmBodyTypeSelector bodyTypeSelector = bodyTypeSelector(Map.of(
				"assign", new StmBodyTypeSelector.EventData("Assign issue", assignPayload),
				"resolve", new StmBodyTypeSelector.EventData("Resolve issue", resolvePayload)));

		List<ChenilePolymorphVariant> variants =
				new ProcessIdPolymorph("issue", bodyTypeSelector).getVariants(null, operationDefinition());

		assertEquals(2, variants.size());
		Map<String, ChenilePolymorphVariant> variantsBySuffix = variantsBySuffix(variants);
		assertVariant(variantsBySuffix.get("issue_assign"), "Assign issue", "assign", assignPayload);
		assertVariant(variantsBySuffix.get("issue_resolve"), "Resolve issue", "resolve", resolvePayload);
	}

	@Test
	void returnsNoVariantsWhenWorkflowHasNoTypedEvents() {
		StmBodyTypeSelector bodyTypeSelector = bodyTypeSelector(Map.of());

		List<ChenilePolymorphVariant> variants =
				new ProcessIdPolymorph("issue", bodyTypeSelector).getVariants(null, operationDefinition());

		assertTrue(variants.isEmpty());
	}

	@Test
	void preservesWorkflowEventIterationOrder() {
		TypeReference<?> firstPayload = typeReference(AssignPayload.class);
		TypeReference<?> secondPayload = typeReference(ResolvePayload.class);
		Map<String, StmBodyTypeSelector.EventData> configs = new LinkedHashMap<>();
		configs.put("first", new StmBodyTypeSelector.EventData("First event", firstPayload));
		configs.put("second", new StmBodyTypeSelector.EventData("Second event", secondPayload));
		StmBodyTypeSelector bodyTypeSelector = bodyTypeSelector(configs);

		List<ChenilePolymorphVariant> variants =
				new ProcessIdPolymorph("workflow", bodyTypeSelector).getVariants(null, operationDefinition());

		assertEquals(List.of("workflow_first", "workflow_second"),
				variants.stream().map(ChenilePolymorphVariant::nameSuffix).toList());
	}

	private void assertVariant(ChenilePolymorphVariant variant, String description, String eventId,
			TypeReference<?> payloadType) {
		assertEquals(description, variant.description());
		assertEquals(Map.of("eventID", eventId), variant.fixedParameterValues());
		assertSame(payloadType, variant.parameterTypes().get("payload"));
		assertEquals(Map.of("payload", description), variant.parameterDescriptions());
		assertTrue(variant.parameterSchemas().isEmpty());
	}

	private Map<String, ChenilePolymorphVariant> variantsBySuffix(List<ChenilePolymorphVariant> variants) {
		Map<String, ChenilePolymorphVariant> variantsBySuffix = new LinkedHashMap<>();
		variants.forEach(variant -> variantsBySuffix.put(variant.nameSuffix(), variant));
		return variantsBySuffix;
	}

	private StmBodyTypeSelector bodyTypeSelector(Map<String, StmBodyTypeSelector.EventData> configs) {
		StmBodyTypeSelector bodyTypeSelector = Mockito.mock(StmBodyTypeSelector.class);
		when(bodyTypeSelector.getConfigs()).thenReturn(configs);
		return bodyTypeSelector;
	}

	private OperationDefinition operationDefinition() {
		OperationDefinition operationDefinition = new OperationDefinition();
		ParamDefinition idParam = param("id");
		ParamDefinition eventParam = param("eventID");
		ParamDefinition payloadParam = param("payload");
		operationDefinition.setParams(List.of(idParam, eventParam, payloadParam));
		return operationDefinition;
	}

	private ParamDefinition param(String name) {
		ParamDefinition paramDefinition = new ParamDefinition();
		paramDefinition.setName(name);
		return paramDefinition;
	}

	private TypeReference<?> typeReference(Class<?> payloadClass) {
		return new TypeReference<>() {
			@Override
			public Type getType() {
				return payloadClass;
			}
		};
	}

	private static class AssignPayload {
	}

	private static class ResolvePayload {
	}
}
