package org.chenile.workflow.service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.media.Schema;
import org.chenile.core.model.ChenileConfiguration;
import org.chenile.core.model.ChenileServiceDefinition;
import org.chenile.core.model.HTTPMethod;
import org.chenile.core.model.HttpBindingType;
import org.chenile.core.model.OperationDefinition;
import org.chenile.core.model.ParamDefinition;
import org.chenile.workflow.service.stmcmds.StmBodyTypeSelector;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationContext;
import tools.jackson.core.type.TypeReference;

import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ChenileOpenApiConfigurationTest {

    @Test
    void createsEventSpecificOpenApiPathsWithConcretePayloadSchemas() {
        StmBodyTypeSelector selector = bodyTypeSelector(Map.of(
                "assign", new StmBodyTypeSelector.EventData("Assign issue", typeReference(AssignPayload.class)),
                "resolve", new StmBodyTypeSelector.EventData("Resolve issue", typeReference(ResolvePayload.class))));
        OpenAPI openAPI = customizeOpenApi(selector, processByIdOperation("PUT", "/issue/{id}/{eventID}"));

        Operation assignOperation = openAPI.getPaths().get("/issue/{id}/assign").getPut();
        Operation resolveOperation = openAPI.getPaths().get("/issue/{id}/resolve").getPut();

        assertNotNull(assignOperation);
        assertNotNull(resolveOperation);
        assertEquals("issueCommandService_processById_assign", assignOperation.getOperationId());
        assertEquals("Assign issue", assignOperation.getSummary());
        assertEquals("id", assignOperation.getParameters().get(0).getName());
        assertEquals("path", assignOperation.getParameters().get(0).getIn());
        assertTrue(assignOperation.getParameters().stream().noneMatch(parameter -> "eventID".equals(parameter.getName())));
        assertSchemaRef(assignOperation, "AssignPayload");
        assertSchemaRef(resolveOperation, "ResolvePayload");
        assertTrue(openAPI.getComponents().getSchemas().containsKey("AssignPayload"));
        assertTrue(openAPI.getComponents().getSchemas().containsKey("ResolvePayload"));
        assertFalse(openAPI.getPaths().keySet().stream().anyMatch(path -> path.matches("/\\d+/.*")));
    }

    @Test
    void usesOriginalHttpMethodForGeneratedEventPath() {
        StmBodyTypeSelector selector = bodyTypeSelector(Map.of(
                "submit", new StmBodyTypeSelector.EventData("Submit", typeReference(AssignPayload.class))));
        OpenAPI openAPI = customizeOpenApi(selector, processByIdOperation("PATCH", "/mfg/{id}/{eventID}"));

        assertNotNull(openAPI.getPaths().get("/mfg/{id}/submit").getPatch());
        assertNull(openAPI.getPaths().get("/mfg/{id}/submit").getPut());
    }

    @Test
    void doesNotOverwriteExistingExplicitOpenApiPath() {
        StmBodyTypeSelector selector = bodyTypeSelector(Map.of(
                "assign", new StmBodyTypeSelector.EventData("Assign issue", typeReference(AssignPayload.class))));
        OpenAPI openAPI = new OpenAPI().paths(new Paths());
        Operation explicitOperation = new Operation().operationId("explicit_assign");
        openAPI.getPaths().addPathItem("/issue/{id}/assign", new PathItem().put(explicitOperation));

        customizer(selector, processByIdOperation("PUT", "/issue/{id}/{eventID}")).activityOpenApi().customise(openAPI);

        assertEquals("explicit_assign", openAPI.getPaths().get("/issue/{id}/assign").getPut().getOperationId());
    }

    @Test
    void skipsBodySelectorBeansThatAreNotStmBodyTypeSelectors() {
        ApplicationContext applicationContext = mock(ApplicationContext.class);
        when(applicationContext.getBean("notWorkflowSelector")).thenReturn(new Object());
        ChenileConfiguration chenileConfiguration = new ChenileConfiguration("test", applicationContext);
        OperationDefinition operationDefinition = processByIdOperation("PUT", "/issue/{id}/{eventID}");
        operationDefinition.setBodyTypeSelectorComponentNames(new String[] { "notWorkflowSelector" });
        chenileConfiguration.setService("issueCommandService", serviceDefinition(operationDefinition));

        OpenAPI openAPI = new OpenAPI();
        ChenileOpenApiConfiguration configuration = configuration(chenileConfiguration, applicationContext);
        configuration.activityOpenApi().customise(openAPI);

        assertTrue(openAPI.getPaths().isEmpty());
    }

    @Test
    void generatesPathsWhenChenileConfigurationIsPopulatedAfterCustomizerBeanCreation() {
        StmBodyTypeSelector selector = bodyTypeSelector(Map.of(
                "assign", new StmBodyTypeSelector.EventData("Assign issue", typeReference(AssignPayload.class))));
        ApplicationContext applicationContext = mock(ApplicationContext.class);
        when(applicationContext.getBean("issueBodyTypeSelector")).thenReturn(selector);
        ChenileConfiguration chenileConfiguration = new ChenileConfiguration("test", applicationContext);
        ChenileOpenApiConfiguration configuration = configuration(chenileConfiguration, applicationContext);
        org.springdoc.core.customizers.OpenApiCustomizer customizer = configuration.activityOpenApi();

        chenileConfiguration.setService("issueCommandService",
                serviceDefinition(processByIdOperation("PUT", "/issue/{id}/{eventID}")));

        OpenAPI openAPI = new OpenAPI();
        customizer.customise(openAPI);

        assertNotNull(openAPI.getPaths().get("/issue/{id}/assign").getPut());
    }

    @Test
    void skipsWhenChenileConfigurationIsUnavailable() {
        @SuppressWarnings("unchecked")
        ObjectProvider<ChenileConfiguration> serviceConfigurationProvider = mock(ObjectProvider.class);
        when(serviceConfigurationProvider.getIfAvailable()).thenReturn(null);
        ChenileOpenApiConfiguration configuration =
                new ChenileOpenApiConfiguration(serviceConfigurationProvider, mock(ApplicationContext.class));

        OpenAPI openAPI = new OpenAPI();
        configuration.activityOpenApi().customise(openAPI);

        assertTrue(openAPI.getPaths().isEmpty());
    }

    @Test
    void skipsMissingBodySelectorBeans() {
        ApplicationContext applicationContext = mock(ApplicationContext.class);
        when(applicationContext.getBean("issueBodyTypeSelector"))
                .thenThrow(new NoSuchBeanDefinitionException("issueBodyTypeSelector"));
        ChenileConfiguration chenileConfiguration = new ChenileConfiguration("test", applicationContext);
        chenileConfiguration.setService("issueCommandService",
                serviceDefinition(processByIdOperation("PUT", "/issue/{id}/{eventID}")));

        OpenAPI openAPI = new OpenAPI();
        configuration(chenileConfiguration, applicationContext).activityOpenApi().customise(openAPI);

        assertTrue(openAPI.getPaths().isEmpty());
    }

    private OpenAPI customizeOpenApi(StmBodyTypeSelector selector, OperationDefinition operationDefinition) {
        OpenAPI openAPI = new OpenAPI();
        customizer(selector, operationDefinition).activityOpenApi().customise(openAPI);
        return openAPI;
    }

    private ChenileOpenApiConfiguration customizer(StmBodyTypeSelector selector, OperationDefinition operationDefinition) {
        ApplicationContext applicationContext = mock(ApplicationContext.class);
        when(applicationContext.getBean("issueBodyTypeSelector")).thenReturn(selector);
        ChenileConfiguration chenileConfiguration = new ChenileConfiguration("test", applicationContext);
        chenileConfiguration.setService("issueCommandService", serviceDefinition(operationDefinition));
        return configuration(chenileConfiguration, applicationContext);
    }

    private ChenileOpenApiConfiguration configuration(ChenileConfiguration chenileConfiguration,
            ApplicationContext applicationContext) {
        @SuppressWarnings("unchecked")
        ObjectProvider<ChenileConfiguration> serviceConfigurationProvider = mock(ObjectProvider.class);
        when(serviceConfigurationProvider.getIfAvailable()).thenReturn(chenileConfiguration);
        return new ChenileOpenApiConfiguration(serviceConfigurationProvider, applicationContext);
    }

    private ChenileServiceDefinition serviceDefinition(OperationDefinition operationDefinition) {
        ChenileServiceDefinition serviceDefinition = new ChenileServiceDefinition();
        serviceDefinition.setName("_issueStateEntityService_");
        serviceDefinition.setId("issueCommandService");
        serviceDefinition.setOperations(List.of(operationDefinition));
        return serviceDefinition;
    }

    private OperationDefinition processByIdOperation(String httpMethod, String url) {
        OperationDefinition operationDefinition = new OperationDefinition();
        operationDefinition.setName("processById");
        operationDefinition.setUrl(url);
        operationDefinition.setHttpMethod(HTTPMethod.valueOf(httpMethod));
        operationDefinition.setBodyTypeSelectorComponentNames(new String[] { "issueBodyTypeSelector" });
        operationDefinition.setParams(List.of(
                param("id", HttpBindingType.HEADER, "Entity id"),
                param("eventID", HttpBindingType.HEADER, "Event id"),
                param("payload", HttpBindingType.BODY, "Event payload")));
        return operationDefinition;
    }

    private ParamDefinition param(String name, HttpBindingType type, String description) {
        ParamDefinition paramDefinition = new ParamDefinition();
        paramDefinition.setName(name);
        paramDefinition.setType(type);
        paramDefinition.setDescription(description);
        return paramDefinition;
    }

    private StmBodyTypeSelector bodyTypeSelector(Map<String, StmBodyTypeSelector.EventData> configs) {
        StmBodyTypeSelector selector = mock(StmBodyTypeSelector.class);
        when(selector.getConfigs()).thenReturn(new LinkedHashMap<>(configs));
        return selector;
    }

    private TypeReference<?> typeReference(Class<?> payloadType) {
        return new TypeReference<>() {
            @Override
            public Type getType() {
                return payloadType;
            }
        };
    }

    private void assertSchemaRef(Operation operation, String schemaName) {
        @SuppressWarnings("rawtypes")
        Schema schema = operation.getRequestBody().getContent().get("application/json").getSchema();
        assertEquals("#/components/schemas/" + schemaName, schema.get$ref());
    }

    private static class AssignPayload {
        public String assignee;
    }

    private static class ResolvePayload {
        public String comment;
    }
}
