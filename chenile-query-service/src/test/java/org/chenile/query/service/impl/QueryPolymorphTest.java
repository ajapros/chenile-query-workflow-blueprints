package org.chenile.query.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.chenile.core.model.OperationDefinition;
import org.chenile.core.model.ParamDefinition;
import org.chenile.query.model.QueryMetadata;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class QueryPolymorphTest {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    public void queryDefinitionsExposeDiscoveredQueries() throws IOException {
        QueryDefinitions queryDefinitions = queryDefinitions();

        List<QueryMetadata> discovered = queryDefinitions.getAllDiscoveredQueryDefinitions();

        Assertions.assertEquals(2, discovered.size());
        Assertions.assertEquals(List.of("students", "AuthUser.reportees"),
                discovered.stream().map(QueryMetadata::getName).toList());
    }

    @Test
    public void queryPolymorphCreatesSchemaPerQuery() throws Exception {
        QueryDefinitions queryDefinitions = queryDefinitions();
        QueryPolymorph queryPolymorph = new QueryPolymorph(queryDefinitions);

        Map<String, JsonNode> schemasByQueryName = queryPolymorph.getVariants(null, operationDefinition()).stream()
                .collect(Collectors.toMap(
                        variant -> (String) variant.fixedParameterValues().get("queryName"),
                        variant -> readSchema(variant.parameterSchemas().get("searchRequest"))));

        JsonNode studentFilters = schemasByQueryName.get("students").path("properties").path("filters").path("properties");
        Assertions.assertTrue(studentFilters.has("id"));
        Assertions.assertTrue(studentFilters.has("name"));
        Assertions.assertTrue(studentFilters.has("branch"));
        Assertions.assertEquals("array", studentFilters.path("branch").path("type").asText());
        Assertions.assertEquals("string", studentFilters.path("name").path("type").asText());

        JsonNode reporteeFilters = schemasByQueryName.get("AuthUser.reportees").path("properties")
                .path("filters").path("properties");
        Assertions.assertTrue(reporteeFilters.has("userId"));
        Assertions.assertFalse(reporteeFilters.has("authId"));
    }

    private QueryDefinitions queryDefinitions() throws IOException {
        return new QueryDefinitions(new ClassPathResource[]{
                new ClassPathResource("org/chenile/samples/query/service/mapper/student.json")
        });
    }

    private OperationDefinition operationDefinition() {
        OperationDefinition operationDefinition = new OperationDefinition();
        ParamDefinition queryNameParam = new ParamDefinition();
        queryNameParam.setName("queryName");
        queryNameParam.setParamClass(String.class);

        ParamDefinition searchRequestParam = new ParamDefinition();
        searchRequestParam.setName("searchRequest");
        searchRequestParam.setParamClass(Object.class);

        operationDefinition.setParams(List.of(queryNameParam, searchRequestParam));
        return operationDefinition;
    }

    private JsonNode readSchema(String schema) {
        try {
            return OBJECT_MAPPER.readTree(schema);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
