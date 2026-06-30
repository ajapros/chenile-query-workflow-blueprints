package org.chenile.configuration.controller;

import java.util.Map;

import org.chenile.base.response.GenericResponse;
import org.chenile.http.annotation.ChenileController;
import org.chenile.http.handler.ControllerSupport;
import org.chenile.query.model.SearchRequest;
import org.chenile.query.model.SearchResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

/**
 * This sets up a query service automatically. Developers can use this optionally instead of
 * writing the controller themselves. It is recommended to write your own controllers if
 * you want to use additional annotations such as chenile security etc.
 */
@RestController
@ChenileController(value = "chenileMybatisQuery", serviceName = "searchService", serviceModule = "query")
public class QueryController extends ControllerSupport{
	@PostMapping("/q/{queryName}")
	// @InterceptedBy("securityInterceptor")
	 public ResponseEntity<GenericResponse<SearchResponse>> search(
			 HttpServletRequest request, 
			 @PathVariable String queryName,
			 @RequestBody SearchRequest<Map<String, Object>> searchRequest) {
		 return process("search",request,queryName,searchRequest);
	 }
}
