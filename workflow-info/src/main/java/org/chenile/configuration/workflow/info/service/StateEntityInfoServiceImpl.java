package org.chenile.configuration.workflow.info.service;

import org.chenile.base.exception.ErrorNumException;
import org.chenile.stm.impl.STMActionsInfoProvider;
import org.chenile.stm.impl.STMFlowStoreImpl;
import org.chenile.workflow.info.STMFlowStoreInfoHelper;
import org.chenile.workflow.info.model.StateEntityAllowedActionsRequest;
import org.chenile.workflow.info.model.StateEntityInfoRequest;
import org.chenile.workflow.info.model.WorkflowInfoResponse;
import org.chenile.workflow.info.service.StateEntityInfoService;

import java.util.List;
import java.util.Map;

public class StateEntityInfoServiceImpl implements StateEntityInfoService {
    private static final String VALIDATION_ERROR = "7002";
    private final STMFlowStoreInfoHelper infoHelper;

    public StateEntityInfoServiceImpl(STMFlowStoreImpl stmFlowStore, STMActionsInfoProvider actionsInfoProvider) {
        this.infoHelper = new STMFlowStoreInfoHelper(stmFlowStore, actionsInfoProvider);
    }

    @Override
    public WorkflowInfoResponse<byte[]> renderStateDiagram(StateEntityInfoRequest request) {
        try {
            String puml = infoHelper.renderStateDiagram(request == null ? null : request.getStylingPropertiesText());
            return responseOf(PlantUmlToImageConverter.toPng(puml));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public WorkflowInfoResponse<List<String>> allowedActions(StateEntityAllowedActionsRequest request) {
        if (request == null || request.getState() == null || request.getState().isEmpty()) {
            throw new ErrorNumException(400, VALIDATION_ERROR, "State is required for allowed actions.");
        }
        return responseOf(infoHelper.allowedActions(request.getState(), request.getFlowId()));
    }

    @Override
    public WorkflowInfoResponse<Map<String, Object>> toJson(StateEntityInfoRequest request) {
        return responseOf(infoHelper.toMap());
    }

    @Override
    public WorkflowInfoResponse<String> generateTestCases(StateEntityInfoRequest request) {
        try {
            return responseOf(infoHelper.renderTestCases());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public WorkflowInfoResponse<String> visualizeTestCases(StateEntityInfoRequest request) {
        try {
            return responseOf(infoHelper.renderTestPuml());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public WorkflowInfoResponse<Map<String, byte[]>> renderTestsAsStateDiagram(StateEntityInfoRequest request) {
        try {
            return responseOf(PlantUmlToImageConverter.toPngMap(infoHelper.visualizeTestcaseAsStateDiagram()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private <T> WorkflowInfoResponse<T> responseOf(T data) {
        return new WorkflowInfoResponse<>(data);
    }
}
