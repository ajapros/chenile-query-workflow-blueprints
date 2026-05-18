package org.chenile.workflow.info.service;

import org.chenile.workflow.info.model.AllowedActionsRequest;
import org.chenile.workflow.info.model.WorkflowInfoRequest;
import org.chenile.workflow.info.model.WorkflowInfoResponse;

import java.util.List;
import java.util.Map;

public interface WorkflowInfoService {
    WorkflowInfoResponse<byte[]> renderStateDiagram(WorkflowInfoRequest request);
    WorkflowInfoResponse<List<String>> allowedActions(AllowedActionsRequest request);
    WorkflowInfoResponse<Map<String,Object>> toJson(WorkflowInfoRequest request);
    WorkflowInfoResponse<String> generateTestCases(WorkflowInfoRequest request);
    WorkflowInfoResponse<String> visualizeTestCases(WorkflowInfoRequest request);
    WorkflowInfoResponse<Map<String,byte[]>> renderTestsAsStateDiagram(WorkflowInfoRequest request);
}
