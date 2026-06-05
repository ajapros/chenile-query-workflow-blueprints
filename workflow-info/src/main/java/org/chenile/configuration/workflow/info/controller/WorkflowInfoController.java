package org.chenile.configuration.workflow.info.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.chenile.base.response.GenericResponse;
import org.chenile.http.annotation.ChenileController;
import org.chenile.http.handler.ControllerSupport;
import org.chenile.workflow.info.model.AllowedActionsRequest;
import org.chenile.workflow.info.model.WorkflowInfoRequest;
import org.chenile.workflow.info.model.WorkflowInfoResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@ChenileController(value = "workflowInfo", serviceName = "workflowInfoService")
public class WorkflowInfoController extends ControllerSupport {
    @RequestMapping(value = "/workflow-info/state-diagram", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<GenericResponse<WorkflowInfoResponse<byte[]>>> renderStateDiagram(
            HttpServletRequest request,
            @RequestBody(required = false) WorkflowInfoRequest workflowInfoRequest,
            @RequestParam(required = false) String xmlText,
            @RequestParam(required = false) String stylingPropertiesText,
            @RequestParam(required = false) String enablementPropertiesText,
            @RequestParam(required = false) String prefix) {
        return process("renderStateDiagram", request,
                mergeWorkflowInfoRequest(workflowInfoRequest, xmlText, stylingPropertiesText, enablementPropertiesText, prefix));
    }

    @RequestMapping(value = "/workflow-info/allowed-actions", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<GenericResponse<WorkflowInfoResponse<List<String>>>> allowedActions(
            HttpServletRequest request,
            @RequestBody(required = false) AllowedActionsRequest workflowInfoRequest,
            @RequestParam(required = false) String xmlText,
            @RequestParam(required = false) String stylingPropertiesText,
            @RequestParam(required = false) String enablementPropertiesText,
            @RequestParam(required = false) String prefix,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String flowId) {
        return process("allowedActions", request,
                mergeAllowedActionsRequest(workflowInfoRequest, xmlText, stylingPropertiesText,
                        enablementPropertiesText, prefix, state, flowId));
    }

    @RequestMapping(value = "/workflow-info/json", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<GenericResponse<WorkflowInfoResponse<Map<String, Object>>>> toJson(
            HttpServletRequest request,
            @RequestBody(required = false) WorkflowInfoRequest workflowInfoRequest,
            @RequestParam(required = false) String xmlText,
            @RequestParam(required = false) String stylingPropertiesText,
            @RequestParam(required = false) String enablementPropertiesText,
            @RequestParam(required = false) String prefix) {
        return process("toJson", request,
                mergeWorkflowInfoRequest(workflowInfoRequest, xmlText, stylingPropertiesText, enablementPropertiesText, prefix));
    }

    @RequestMapping(value = "/workflow-info/test-cases", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<GenericResponse<WorkflowInfoResponse<String>>> generateTestCases(
            HttpServletRequest request,
            @RequestBody(required = false) WorkflowInfoRequest workflowInfoRequest,
            @RequestParam(required = false) String xmlText,
            @RequestParam(required = false) String stylingPropertiesText,
            @RequestParam(required = false) String enablementPropertiesText,
            @RequestParam(required = false) String prefix) {
        return process("generateTestCases", request,
                mergeWorkflowInfoRequest(workflowInfoRequest, xmlText, stylingPropertiesText, enablementPropertiesText, prefix));
    }

    @RequestMapping(value = "/workflow-info/test-visualization", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<GenericResponse<WorkflowInfoResponse<String>>> visualizeTestCases(
            HttpServletRequest request,
            @RequestBody(required = false) WorkflowInfoRequest workflowInfoRequest,
            @RequestParam(required = false) String xmlText,
            @RequestParam(required = false) String stylingPropertiesText,
            @RequestParam(required = false) String enablementPropertiesText,
            @RequestParam(required = false) String prefix) {
        return process("visualizeTestCases", request,
                mergeWorkflowInfoRequest(workflowInfoRequest, xmlText, stylingPropertiesText, enablementPropertiesText, prefix));
    }

    @RequestMapping(value = "/workflow-info/test-state-diagrams", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<GenericResponse<WorkflowInfoResponse<Map<String, byte[]>>>> renderTestsAsStateDiagram(
            HttpServletRequest request,
            @RequestBody(required = false) WorkflowInfoRequest workflowInfoRequest,
            @RequestParam(required = false) String xmlText,
            @RequestParam(required = false) String stylingPropertiesText,
            @RequestParam(required = false) String enablementPropertiesText,
            @RequestParam(required = false) String prefix) {
        return process("renderTestsAsStateDiagram", request,
                mergeWorkflowInfoRequest(workflowInfoRequest, xmlText, stylingPropertiesText, enablementPropertiesText, prefix));
    }

    private WorkflowInfoRequest mergeWorkflowInfoRequest(WorkflowInfoRequest request, String xmlText,
                                                         String stylingPropertiesText,
                                                         String enablementPropertiesText, String prefix) {
        WorkflowInfoRequest workflowInfoRequest = request == null ? new WorkflowInfoRequest() : request;
        if (xmlText != null) {
            workflowInfoRequest.setXmlText(xmlText);
        }
        if (stylingPropertiesText != null) {
            workflowInfoRequest.setStylingPropertiesText(stylingPropertiesText);
        }
        if (enablementPropertiesText != null) {
            workflowInfoRequest.setEnablementPropertiesText(enablementPropertiesText);
        }
        if (prefix != null) {
            workflowInfoRequest.setPrefix(prefix);
        }
        return workflowInfoRequest;
    }

    private AllowedActionsRequest mergeAllowedActionsRequest(AllowedActionsRequest request, String xmlText,
                                                             String stylingPropertiesText,
                                                             String enablementPropertiesText, String prefix,
                                                             String state, String flowId) {
        AllowedActionsRequest allowedActionsRequest =
                request == null ? new AllowedActionsRequest() : request;
        mergeWorkflowInfoRequest(allowedActionsRequest, xmlText, stylingPropertiesText, enablementPropertiesText, prefix);
        if (state != null) {
            allowedActionsRequest.setState(state);
        }
        if (flowId != null) {
            allowedActionsRequest.setFlowId(flowId);
        }
        return allowedActionsRequest;
    }
}
