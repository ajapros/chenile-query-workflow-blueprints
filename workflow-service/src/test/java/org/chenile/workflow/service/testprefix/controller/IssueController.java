package org.chenile.workflow.service.testprefix.controller;


import jakarta.servlet.http.HttpServletRequest;
import org.chenile.base.response.GenericResponse;
import org.chenile.http.annotation.BodyTypeSelector;
import org.chenile.http.annotation.ChenileController;
import org.chenile.http.annotation.ChenileParamType;
import org.chenile.http.handler.ControllerSupport;
import org.chenile.stm.StateEntity;
import org.chenile.workflow.dto.StateEntityServiceResponse;
import org.chenile.workflow.service.testprefix.issues.Issue;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@ChenileController(value = "issueService", serviceName = "_issueStateEntityService_")
public class IssueController extends ControllerSupport{
	
	@GetMapping("/tissue/{id}")
	public ResponseEntity<GenericResponse<StateEntityServiceResponse<Issue>>> retrieve(
			HttpServletRequest httpServletRequest,
			@PathVariable String id){
		return process(httpServletRequest,id);
	}

	@PostMapping("/tissue")
	public ResponseEntity<GenericResponse<StateEntityServiceResponse<Issue>>> create(
			HttpServletRequest httpServletRequest,
			@ChenileParamType(StateEntity.class)
			@RequestBody Issue entity){
		return process(httpServletRequest,entity);
	}

	
	@PutMapping("/tissue/{id}/{eventID}")
	@BodyTypeSelector("issueBodyTypeSelector")
	public ResponseEntity<GenericResponse<StateEntityServiceResponse<Issue>>> processById(
			HttpServletRequest httpServletRequest,
			@PathVariable String id,
			@PathVariable String eventID,
			@ChenileParamType(Object.class) 
			@RequestBody String eventPayload){
		return process(httpServletRequest,id,eventID,eventPayload);
	}


}
