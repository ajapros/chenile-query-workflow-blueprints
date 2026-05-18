package org.chenile.configuration.workflow.info.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.chenile.base.response.GenericResponse;
import org.chenile.http.annotation.ChenileController;
import org.chenile.http.handler.ControllerSupport;
import org.chenile.workflow.info.model.AllowedActionsRequest;
import org.chenile.workflow.info.model.WorkflowInfoRequest;
import org.chenile.workflow.info.model.WorkflowInfoResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@ChenileController(value = "workflowInfo", serviceName = "workflowInfoService")
public class WorkflowInfoController extends ControllerSupport {
    @PostMapping("/workflow-info/state-diagram")
    public ResponseEntity<GenericResponse<WorkflowInfoResponse<byte[]>>> renderStateDiagram(
            HttpServletRequest request,
            @RequestBody WorkflowInfoRequest workflowInfoRequest) {
        return process("renderStateDiagram", request, workflowInfoRequest);
    }

    @PostMapping("/workflow-info/allowed-actions")
    public ResponseEntity<GenericResponse<WorkflowInfoResponse<List<String>>>> allowedActions(
            HttpServletRequest request,
            @RequestBody AllowedActionsRequest workflowInfoRequest) {
        return process("allowedActions", request, workflowInfoRequest);
    }

    @PostMapping("/workflow-info/json")
    public ResponseEntity<GenericResponse<WorkflowInfoResponse<Map<String, Object>>>> toJson(
            HttpServletRequest request,
            @RequestBody WorkflowInfoRequest workflowInfoRequest) {
        return process("toJson", request, workflowInfoRequest);
    }

    @PostMapping("/workflow-info/test-cases")
    public ResponseEntity<GenericResponse<WorkflowInfoResponse<String>>> generateTestCases(
            HttpServletRequest request,
            @RequestBody WorkflowInfoRequest workflowInfoRequest) {
        return process("generateTestCases", request, workflowInfoRequest);
    }

    @PostMapping("/workflow-info/test-visualization")
    public ResponseEntity<GenericResponse<WorkflowInfoResponse<String>>> visualizeTestCases(
            HttpServletRequest request,
            @RequestBody WorkflowInfoRequest workflowInfoRequest) {
        return process("visualizeTestCases", request, workflowInfoRequest);
    }

    @PostMapping("/workflow-info/test-state-diagrams")
    public ResponseEntity<GenericResponse<WorkflowInfoResponse<Map<String, byte[]>>>> renderTestsAsStateDiagram(
            HttpServletRequest request,
            @RequestBody WorkflowInfoRequest workflowInfoRequest) {
        return process("renderTestsAsStateDiagram", request, workflowInfoRequest);
    }
}
