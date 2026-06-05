package org.chenile.configuration.query.mcp;

import jakarta.servlet.http.HttpServletRequest;
import org.chenile.mcp.model.ChenileMCP;
import org.chenile.mcp.model.ChenilePolymorph;
import org.chenile.query.model.SearchRequest;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class QueryMCPControllerTest {

    @Test
    void mcpQueryControllerExposesMcpAnnotations() throws NoSuchMethodException {
        Method search = QueryMCPController.class.getDeclaredMethod("search",
                HttpServletRequest.class, String.class, SearchRequest.class);

        ChenileMCP chenileMCP = search.getAnnotation(ChenileMCP.class);
        ChenilePolymorph chenilePolymorph = search.getAnnotation(ChenilePolymorph.class);

        assertNotNull(chenileMCP);
        assertEquals("querySearch", chenileMCP.name());
        assertNotNull(chenilePolymorph);
        assertEquals("queryPolymorph", chenilePolymorph.value());
    }
}
