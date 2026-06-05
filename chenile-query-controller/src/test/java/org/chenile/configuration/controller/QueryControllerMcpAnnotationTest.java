package org.chenile.configuration.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.chenile.query.model.SearchRequest;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertFalse;

class QueryControllerMcpAnnotationTest {

    @Test
    void baseQueryControllerDoesNotExposeMcpAnnotations() throws NoSuchMethodException {
        Method search = QueryController.class.getDeclaredMethod("search",
                HttpServletRequest.class, String.class, SearchRequest.class);

        for (Annotation annotation : search.getAnnotations()) {
            String annotationType = annotation.annotationType().getName();
            assertFalse(annotationType.equals("org.chenile.mcp.model.ChenileMCP"));
            assertFalse(annotationType.equals("org.chenile.mcp.model.ChenilePolymorph"));
        }
    }
}
