package org.chenile.workflow.service.testmulti.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.chenile.base.response.GenericResponse;
import org.chenile.http.annotation.BodyTypeSelector;
import org.chenile.http.annotation.ChenileController;
import org.chenile.http.annotation.ChenileParamType;
import org.chenile.http.handler.ControllerSupport;
import org.chenile.stm.StateEntity;
import org.chenile.workflow.dto.StateEntityServiceResponse;
import org.chenile.workflow.service.testmulti.model.Vehicle;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@ChenileController(value = "vehicleService", serviceName = "_vehicleStateEntityService_")
public class VehicleController extends ControllerSupport {

	@GetMapping("/vehicles/{id}")
	public ResponseEntity<GenericResponse<StateEntityServiceResponse<Vehicle>>> retrieve(
			HttpServletRequest httpServletRequest,
			@PathVariable String id) {
		return process(httpServletRequest, id);
	}

	@PostMapping("/vehicles")
	public ResponseEntity<GenericResponse<StateEntityServiceResponse<Vehicle>>> create(
			HttpServletRequest httpServletRequest,
			@ChenileParamType(StateEntity.class) @RequestBody Vehicle entity) {
		return process(httpServletRequest, entity);
	}

	@PutMapping("/vehicles/{id}/{eventID}")
	@BodyTypeSelector("vehicleBodyTypeSelector")
	public ResponseEntity<GenericResponse<StateEntityServiceResponse<Vehicle>>> processById(
			HttpServletRequest httpServletRequest,
			@PathVariable String id,
			@PathVariable String eventID,
			@ChenileParamType(Object.class) @RequestBody String eventPayload) {
		return process(httpServletRequest, id, eventID, eventPayload);
	}
}
