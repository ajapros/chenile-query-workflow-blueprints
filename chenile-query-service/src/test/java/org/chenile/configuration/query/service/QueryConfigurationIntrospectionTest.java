package org.chenile.configuration.query.service;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;

class QueryConfigurationIntrospectionTest {

    @Test
    void queryConfigurationCanBeIntrospectedWithoutMcpTypes() {
        Method[] methods = assertDoesNotThrow(QueryConfiguration.class::getDeclaredMethods);

        assertFalse(Arrays.stream(methods)
                .map(Method::getReturnType)
                .map(Class::getName)
                .anyMatch(typeName -> typeName.contains("Polymorph") || typeName.contains(".mcp.")));
    }
}
