package org.chenile.workflow.service.test1.controller;


import jakarta.servlet.http.HttpServletRequest;
import org.chenile.base.response.GenericResponse;
import org.chenile.http.annotation.BodyTypeSelector;
import org.chenile.http.annotation.ChenileController;
import org.chenile.http.annotation.ChenileParamType;
import org.chenile.http.handler.ControllerSupport;
import org.chenile.stm.StateEntity;
import org.chenile.workflow.dto.StateEntityServiceResponse;
import org.chenile.workflow.service.test1.mfg.MfgModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@ChenileController(value = "mfgService", serviceName = "_mfgStateEntityService_")
public class MfgController extends ControllerSupport{
	
	@GetMapping("/mfg/{id}")
	public ResponseEntity<GenericResponse<StateEntityServiceResponse<MfgModel>>> retrieve(
			HttpServletRequest httpServletRequest,
			@PathVariable String id){
		return process(httpServletRequest,id);
	}

	@PostMapping("/mfg")
	public ResponseEntity<GenericResponse<StateEntityServiceResponse<MfgModel>>> create(
			HttpServletRequest httpServletRequest,
			@ChenileParamType(StateEntity.class)
			@RequestBody MfgModel entity){
		return process(httpServletRequest,entity);
	}

	
	@PatchMapping("/mfg/{id}/{eventID}")
	@BodyTypeSelector("mfgBodyTypeSelector")
	public ResponseEntity<GenericResponse<StateEntityServiceResponse<MfgModel>>> processById(
			HttpServletRequest httpServletRequest,
			@PathVariable String id,
			@PathVariable String eventID,
			@ChenileParamType(Object.class) 
			@RequestBody String eventPayload){
		return process(httpServletRequest,id,eventID,eventPayload);
	}


}
