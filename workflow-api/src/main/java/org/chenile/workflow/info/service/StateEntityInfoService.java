package org.chenile.workflow.info.service;

import org.chenile.workflow.info.model.StateEntityAllowedActionsRequest;
import org.chenile.workflow.info.model.StateEntityInfoRequest;
import org.chenile.workflow.info.model.WorkflowInfoResponse;

import java.util.List;
import java.util.Map;

public interface StateEntityInfoService {
    WorkflowInfoResponse<String> renderStateDiagram(StateEntityInfoRequest request);
    WorkflowInfoResponse<List<String>> allowedActions(StateEntityAllowedActionsRequest request);
    WorkflowInfoResponse<Map<String,Object>> toJson(StateEntityInfoRequest request);
    WorkflowInfoResponse<String> generateTestCases(StateEntityInfoRequest request);
    WorkflowInfoResponse<String> visualizeTestCases(StateEntityInfoRequest request);
    WorkflowInfoResponse<Map<String,String>> renderTestsAsStateDiagram(StateEntityInfoRequest request);
}
