package org.chenile.workflow.service.config;

import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.chenile.core.model.ChenileConfiguration;
import org.chenile.core.model.ChenileServiceDefinition;
import org.chenile.core.model.HttpBindingType;
import org.chenile.core.model.OperationDefinition;
import org.chenile.core.model.ParamDefinition;
import org.chenile.workflow.service.stmcmds.StmBodyTypeSelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Configuration
public class ChenileOpenApiConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(ChenileOpenApiConfiguration.class);
    private static final String APPLICATION_JSON = "application/json";
    private static final List<String> EVENT_PARAMETER_NAMES = List.of("eventID", "eventId", "event");

    private final ObjectProvider<ChenileConfiguration> serviceConfigurationProvider;
    private final ApplicationContext applicationContext;

    public ChenileOpenApiConfiguration(ObjectProvider<ChenileConfiguration> serviceConfigurationProvider,
            ApplicationContext applicationContext) {
        this.serviceConfigurationProvider = serviceConfigurationProvider;
        this.applicationContext = applicationContext;
    }

    @Bean
    public OpenApiCustomizer activityOpenApi() {
        return openApi -> {
            ensureOpenApiContainers(openApi);
            ChenileConfiguration serviceConfiguration = serviceConfigurationProvider.getIfAvailable();
            if (serviceConfiguration == null || serviceConfiguration.getServices() == null
                    || serviceConfiguration.getServices().isEmpty()) {
                logger.debug("No Chenile services are available for workflow event OpenAPI path generation.");
                return;
            }
            serviceConfiguration.getServices().forEach((serviceId, serviceDefinition) ->
                    addEventSpecificOperations(openApi, serviceId, serviceDefinition));
        };
    }

    private void ensureOpenApiContainers(io.swagger.v3.oas.models.OpenAPI openApi) {
        if (openApi.getPaths() == null) {
            openApi.setPaths(new Paths());
        }
        if (openApi.getComponents() == null) {
            openApi.setComponents(new Components());
        }
        if (openApi.getComponents().getSchemas() == null) {
            openApi.getComponents().setSchemas(new java.util.LinkedHashMap<>());
        }
    }

    private void addEventSpecificOperations(io.swagger.v3.oas.models.OpenAPI openApi, String serviceId,
            ChenileServiceDefinition serviceDefinition) {
        if (serviceDefinition.getOperations() == null) {
            return;
        }
        for (OperationDefinition operationDefinition : serviceDefinition.getOperations()) {
            List<StmBodyTypeSelector> selectors = selectorsFor(serviceDefinition, operationDefinition);
            if (selectors.isEmpty()) {
                continue;
            }
            Optional<String> eventParameterName = eventParameterName(operationDefinition);
            if (eventParameterName.isEmpty()) {
                logger.warn("Cannot generate event-specific OpenAPI paths for service {} operation {}. "
                                + "No event path parameter was found.",
                        serviceId, operationDefinition.getName());
                continue;
            }
            for (StmBodyTypeSelector selector : selectors) {
                selector.getConfigs().forEach((eventId, eventData) ->
                        addEventPath(openApi, serviceId, operationDefinition, eventParameterName.get(), eventId,
                                eventData));
            }
        }
    }

    private List<StmBodyTypeSelector> selectorsFor(ChenileServiceDefinition serviceDefinition,
            OperationDefinition operationDefinition) {
        List<String> selectorNames = new ArrayList<>();
        if (operationDefinition.getBodyTypeSelectorComponentNames() != null
                && operationDefinition.getBodyTypeSelectorComponentNames().length > 0) {
            selectorNames.addAll(Arrays.asList(operationDefinition.getBodyTypeSelectorComponentNames()));
        } else if (serviceDefinition.getBodyTypeSelectorComponentName() != null
                && !serviceDefinition.getBodyTypeSelectorComponentName().isBlank()) {
            selectorNames.add(serviceDefinition.getBodyTypeSelectorComponentName());
        }

        List<StmBodyTypeSelector> selectors = new ArrayList<>();
        for (String selectorName : selectorNames) {
            Object bean;
            try {
                bean = applicationContext.getBean(selectorName);
            } catch (BeansException e) {
                logger.warn("Body type selector bean {} is not available. Skipping workflow OpenAPI event path "
                        + "generation for it.", selectorName);
                logger.debug("Body type selector bean lookup failed for {}", selectorName, e);
                continue;
            }
            if (bean instanceof StmBodyTypeSelector selector) {
                selectors.add(selector);
            } else {
                logger.warn("Body type selector bean {} is not an StmBodyTypeSelector. Skipping workflow OpenAPI "
                        + "event path generation for it.", selectorName);
            }
        }
        return selectors;
    }

    private Optional<String> eventParameterName(OperationDefinition operationDefinition) {
        String url = operationDefinition.getUrl();
        for (String candidate : EVENT_PARAMETER_NAMES) {
            if (url != null && url.contains("{" + candidate + "}")) {
                return Optional.of(candidate);
            }
        }
        List<ParamDefinition> pathOrHeaderParams = operationDefinition.getParams() == null
                ? List.of()
                : operationDefinition.getParams().stream()
                        .filter(param -> param.getType() != HttpBindingType.BODY)
                        .toList();
        if (pathOrHeaderParams.size() > 1) {
            return Optional.of(pathOrHeaderParams.get(1).getName());
        }
        return Optional.empty();
    }

    private void addEventPath(io.swagger.v3.oas.models.OpenAPI openApi, String serviceId,
            OperationDefinition operationDefinition, String eventParameterName, String eventId,
            StmBodyTypeSelector.EventData eventData) {
        String eventPath = eventPath(operationDefinition.getUrl(), eventParameterName, eventId);
        if (eventPath == null) {
            return;
        }
        PathItem.HttpMethod httpMethod = httpMethod(operationDefinition);
        PathItem pathItem = openApi.getPaths().get(eventPath);
        if (pathItem != null && pathItem.readOperationsMap().containsKey(httpMethod)) {
            logger.warn("OpenAPI path {} {} already exists. Skipping generated workflow event path.", httpMethod,
                    eventPath);
            return;
        }
        if (pathItem == null) {
            pathItem = new PathItem();
            openApi.getPaths().addPathItem(eventPath, pathItem);
        }
        operation(pathItem, httpMethod, buildEventOperation(openApi, serviceId, operationDefinition,
                eventParameterName, eventId, eventData));
    }

    private String eventPath(String operationUrl, String eventParameterName, String eventId) {
        if (operationUrl == null || operationUrl.isBlank()) {
            return null;
        }
        String eventPlaceholder = "{" + eventParameterName + "}";
        if (!operationUrl.contains(eventPlaceholder)) {
            logger.warn("Cannot generate event-specific OpenAPI path for {}. Event parameter {} is not in URL.",
                    operationUrl, eventParameterName);
            return null;
        }
        return operationUrl.replace(eventPlaceholder, eventId);
    }

    private Operation buildEventOperation(io.swagger.v3.oas.models.OpenAPI openApi, String serviceId,
            OperationDefinition operationDefinition, String eventParameterName, String eventId,
            StmBodyTypeSelector.EventData eventData) {
        Operation operation = new Operation()
                .operationId(operationId(serviceId, operationDefinition.getName(), eventId))
                .summary(eventData.description() == null ? operationDefinition.getName() + " " + eventId
                        : eventData.description())
                .description(operationDefinition.getDescription())
                .requestBody(
                        new io.swagger.v3.oas.models.parameters.RequestBody()
                                .required(true)
                                .content(new Content().addMediaType(
                                        APPLICATION_JSON,
                                        new MediaType().schema(payloadSchema(openApi, eventData.typeReference().getType()))
                                ))
                )
                .responses(new ApiResponses()
                        .addApiResponse("200", new ApiResponse().description("OK"))
                );
        addPathParameters(operation, operationDefinition, eventParameterName);
        return operation;
    }

    @SuppressWarnings("rawtypes")
    private Schema payloadSchema(io.swagger.v3.oas.models.OpenAPI openApi, Type payloadType) {
        Map<String, Schema> schemas = ModelConverters.getInstance().read(payloadType);
        schemas.forEach((schemaName, schema) -> openApi.getComponents().addSchemas(schemaName, schema));
        return schemas.keySet().stream()
                .findFirst()
                .<Schema>map(schemaName -> new Schema<>().$ref("#/components/schemas/" + schemaName))
                .orElseGet(() -> new Schema<>().type("object"));
    }

    private void addPathParameters(Operation operation, OperationDefinition operationDefinition,
            String eventParameterName) {
        if (operationDefinition.getParams() == null || operationDefinition.getUrl() == null) {
            return;
        }
        operationDefinition.getParams().stream()
                .filter(param -> param.getType() != HttpBindingType.BODY)
                .filter(param -> !param.getName().equals(eventParameterName))
                .filter(param -> operationDefinition.getUrl().contains("{" + param.getName() + "}"))
                .forEach(param -> operation.addParametersItem(new Parameter()
                        .name(param.getName())
                        .in("path")
                        .required(true)
                        .description(param.getDescription())
                        .schema(new Schema<>().type("string"))));
    }

    private PathItem.HttpMethod httpMethod(OperationDefinition operationDefinition) {
        if (operationDefinition.getHttpMethod() == null) {
            return PathItem.HttpMethod.POST;
        }
        return PathItem.HttpMethod.valueOf(operationDefinition.getHttpMethod().name());
    }

    private void operation(PathItem pathItem, PathItem.HttpMethod httpMethod, Operation operation) {
        switch (httpMethod) {
            case GET -> pathItem.get(operation);
            case PUT -> pathItem.put(operation);
            case POST -> pathItem.post(operation);
            case DELETE -> pathItem.delete(operation);
            case PATCH -> pathItem.patch(operation);
            default -> throw new IllegalArgumentException("Unsupported OpenAPI HTTP method " + httpMethod);
        }
    }

    private String operationId(String serviceId, String operationName, String eventId) {
        return (serviceId + "_" + operationName + "_" + eventId).replaceAll("[^A-Za-z0-9_]", "_");
    }

}
