package org.chenile.query.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.chenile.core.model.ChenileServiceDefinition;
import org.chenile.core.model.OperationDefinition;
import org.chenile.core.model.ParamDefinition;
import org.chenile.mcp.model.ChenilePolymorphProvider;
import org.chenile.mcp.model.ChenilePolymorphVariant;
import org.chenile.query.model.ColumnMetadata;
import org.chenile.query.model.QueryMetadata;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Exposes one MCP tool variant per discovered query definition.
 */
public class QueryPolymorph implements ChenilePolymorphProvider {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final QueryDefinitions queryDefinitions;

    public QueryPolymorph(QueryDefinitions queryDefinitions) {
        this.queryDefinitions = queryDefinitions;
    }

    @Override
    public List<ChenilePolymorphVariant> getVariants(ChenileServiceDefinition serviceDefinition,
                                                     OperationDefinition operationDefinition) {
        String queryNameParam = parameterName(operationDefinition, 0, "queryName");
        String searchRequestParam = parameterName(operationDefinition, 1, "searchRequest");
        List<ChenilePolymorphVariant> variants = new ArrayList<>();
        for (QueryMetadata queryMetadata : queryDefinitions.getAllDiscoveredQueryDefinitions()) {
            variants.add(new ChenilePolymorphVariant(
                    sanitizeVariantSuffix(queryMetadata.getName()),
                    "Execute query " + queryMetadata.getName(),
                    Map.of(),
                    Map.of(searchRequestParam, buildSearchRequestSchema(queryMetadata)),
                    Map.of(searchRequestParam, "Search request for query " + queryMetadata.getName()),
                    Map.of(queryNameParam, queryMetadata.getName())
            ));
        }
        return variants;
    }

    private String parameterName(OperationDefinition operationDefinition, int index, String fallback) {
        List<ParamDefinition> params = operationDefinition == null ? null : operationDefinition.getParams();
        if (params == null || params.size() <= index) {
            return fallback;
        }
        String paramName = params.get(index).getName();
        return (paramName == null || paramName.isBlank()) ? fallback : paramName;
    }

    private String sanitizeVariantSuffix(String queryName) {
        return queryName.replaceAll("[^A-Za-z0-9_]", "_");
    }

    String buildSearchRequestSchema(QueryMetadata queryMetadata) {
        ObjectNode root = OBJECT_MAPPER.createObjectNode();
        root.put("type", "object");
        ObjectNode properties = root.putObject("properties");

        if (queryMetadata.isPaginated()) {
            properties.set("pageNum", integerNode("Requested page number"));
            properties.set("numRowsInPage", integerNode("Rows requested per page"));
        }
        if (queryMetadata.isSortable()) {
            properties.set("sortCriteria", sortCriteriaNode(queryMetadata));
        }
        if (queryMetadata.isToDoList()) {
            properties.set("toDoList", booleanNode("Restrict results to current user's to-do list"));
        }
        properties.set("fields", stringArrayNode("Columns to include in the response"));
        properties.set("hiddenColumns", stringArrayNode("Columns to hide in the response"));
        properties.set("cannedReportName", stringNode("Optional canned report name"));
        properties.set("customVariables", freeFormObjectNode("Additional query variables"));
        properties.set("orOperation", booleanNode("Combine filters using OR instead of AND"));
        properties.set("filters", filtersNode(queryMetadata));

        return root.toString();
    }

    private ObjectNode filtersNode(QueryMetadata queryMetadata) {
        ObjectNode filters = OBJECT_MAPPER.createObjectNode();
        filters.put("type", "object");
        filters.put("description", "Filter values for query " + queryMetadata.getName());
        ObjectNode properties = filters.putObject("properties");
        Map<String, ColumnMetadata> columnMetadataMap = queryMetadata.getColumnMetadata();
        if (columnMetadataMap == null || columnMetadataMap.isEmpty()) {
            return filters;
        }
        columnMetadataMap.entrySet().stream()
                .filter(entry -> entry.getValue() != null && entry.getValue().isFilterable())
                .sorted(Map.Entry.comparingByKey(Comparator.naturalOrder()))
                .forEach(entry -> properties.set(entry.getKey(), filterSchema(entry.getKey(), entry.getValue())));
        return filters;
    }

    private ObjectNode filterSchema(String columnName, ColumnMetadata columnMetadata) {
        if (columnMetadata.isBetweenQuery()) {
            ObjectNode node = OBJECT_MAPPER.createObjectNode();
            node.put("type", "array");
            node.put("minItems", 1);
            node.put("maxItems", 2);
            node.put("description", descriptionFor(columnName, columnMetadata));
            node.set("items", scalarSchema(columnMetadata));
            return node;
        }
        if (columnMetadata.isContainsQuery()) {
            ObjectNode node = OBJECT_MAPPER.createObjectNode();
            node.put("type", "array");
            node.put("description", descriptionFor(columnName, columnMetadata));
            node.set("items", scalarSchema(columnMetadata));
            return node;
        }
        ObjectNode node = scalarSchema(columnMetadata);
        node.put("description", descriptionFor(columnName, columnMetadata));
        return node;
    }

    private ObjectNode scalarSchema(ColumnMetadata columnMetadata) {
        ObjectNode node = OBJECT_MAPPER.createObjectNode();
        ColumnMetadata.ColumnType columnType = columnMetadata.getColumnType();
        if (columnType == null) {
            node.put("type", "string");
            return node;
        }
        switch (columnType) {
            case CheckBox -> node.put("type", "boolean");
            case Number -> node.put("type", "number");
            case DropDown -> {
                node.put("type", "string");
                if (columnMetadata.getDropDownValues() != null && !columnMetadata.getDropDownValues().isEmpty()) {
                    ArrayNode enumNode = node.putArray("enum");
                    columnMetadata.getDropDownValues().forEach(enumNode::add);
                }
            }
            case Date -> {
                node.put("type", "string");
                node.put("format", "date");
            }
            case DateTime -> {
                node.put("type", "string");
                node.put("format", "date-time");
            }
            case Text -> node.put("type", "string");
        }
        return node;
    }

    private String descriptionFor(String columnName, ColumnMetadata columnMetadata) {
        List<String> details = new ArrayList<>();
        details.add("Filter on " + columnName);
        if (columnMetadata.isLikeQuery()) {
            details.add("supports LIKE matching");
        }
        if (columnMetadata.isContainsQuery()) {
            details.add("accepts multiple values");
        }
        if (columnMetadata.isBetweenQuery()) {
            details.add("accepts one or two range values");
        }
        return details.stream().collect(Collectors.joining(". ")) + ".";
    }

    private ObjectNode integerNode(String description) {
        ObjectNode node = OBJECT_MAPPER.createObjectNode();
        node.put("type", "integer");
        node.put("description", description);
        return node;
    }

    private ObjectNode booleanNode(String description) {
        ObjectNode node = OBJECT_MAPPER.createObjectNode();
        node.put("type", "boolean");
        node.put("description", description);
        return node;
    }

    private ObjectNode stringNode(String description) {
        ObjectNode node = OBJECT_MAPPER.createObjectNode();
        node.put("type", "string");
        node.put("description", description);
        return node;
    }

    private ObjectNode stringArrayNode(String description) {
        ObjectNode node = OBJECT_MAPPER.createObjectNode();
        node.put("type", "array");
        node.put("description", description);
        ObjectNode items = node.putObject("items");
        items.put("type", "string");
        return node;
    }

    private ObjectNode freeFormObjectNode(String description) {
        ObjectNode node = OBJECT_MAPPER.createObjectNode();
        node.put("type", "object");
        node.put("description", description);
        node.put("additionalProperties", true);
        return node;
    }

    private ObjectNode sortCriteriaNode(QueryMetadata queryMetadata) {
        ObjectNode node = OBJECT_MAPPER.createObjectNode();
        node.put("type", "array");
        node.put("description", "Optional sort criteria");
        ObjectNode items = node.putObject("items");
        items.put("type", "object");
        ObjectNode properties = items.putObject("properties");
        properties.set("index", integerNode("Optional positional sort index"));
        ObjectNode nameNode = stringNode("Sortable column name");
        List<String> sortableColumns = queryMetadata.getColumnMetadata() == null ? List.of() :
                queryMetadata.getColumnMetadata().entrySet().stream()
                        .filter(entry -> entry.getValue() != null && entry.getValue().isSortable())
                        .map(Map.Entry::getKey)
                        .sorted()
                        .toList();
        if (!sortableColumns.isEmpty()) {
            ArrayNode enumNode = nameNode.putArray("enum");
            sortableColumns.forEach(enumNode::add);
        }
        properties.set("name", nameNode);
        properties.set("ascendingOrder", booleanNode("Sort ascending when true"));
        return node;
    }
}
