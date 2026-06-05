package org.chenile.configuration.query.mcp;

import jakarta.servlet.http.HttpServletRequest;
import org.chenile.base.response.GenericResponse;
import org.chenile.http.annotation.ChenileController;
import org.chenile.http.handler.ControllerSupport;
import org.chenile.mcp.model.ChenileMCP;
import org.chenile.mcp.model.ChenilePolymorph;
import org.chenile.query.model.SearchRequest;
import org.chenile.query.model.SearchResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * MCP-enabled Query controller. Applications that need MCP metadata should use
 * this optional module instead of exposing MCP annotations from the base query
 * controller.
 */
@RestController
@ChenileController(value = "chenileMybatisQuery", serviceName = "searchService")
public class QueryMCPController extends ControllerSupport {

    @PostMapping("/q/{queryName}")
    @ChenileMCP(name = "querySearch", description = "Execute a named Chenile query")
    @ChenilePolymorph("queryPolymorph")
    public ResponseEntity<GenericResponse<SearchResponse>> search(
            HttpServletRequest request,
            @PathVariable String queryName,
            @RequestBody SearchRequest<Map<String, Object>> searchRequest) {
        return process("search", request, queryName, searchRequest);
    }
}
