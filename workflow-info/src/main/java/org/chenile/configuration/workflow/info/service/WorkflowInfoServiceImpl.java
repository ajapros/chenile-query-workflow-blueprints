package org.chenile.configuration.workflow.info.service;

import org.chenile.base.exception.ErrorNumException;
import org.chenile.workflow.cli.CLIHelper;
import org.chenile.workflow.cli.CLIParams;
import org.chenile.workflow.info.model.AllowedActionsRequest;
import org.chenile.workflow.info.model.WorkflowInfoRequest;
import org.chenile.workflow.info.model.WorkflowInfoResponse;
import org.chenile.workflow.info.service.WorkflowInfoService;

import java.util.List;
import java.util.Map;

public class WorkflowInfoServiceImpl implements WorkflowInfoService {
    private static final int VALIDATION_ERROR = 7001;
    private final CLIHelper cliHelper;

    public WorkflowInfoServiceImpl(CLIHelper cliHelper) {
        this.cliHelper = cliHelper;
    }

    @Override
    public WorkflowInfoResponse<byte[]> renderStateDiagram(WorkflowInfoRequest request) {
        validateXmlText(request);
        try {
            return responseOf(PlantUmlToImageConverter.toPng(cliHelper.renderStateDiagram(toCliParams(request))));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public WorkflowInfoResponse<List<String>> allowedActions(AllowedActionsRequest request) {
        validateXmlText(request);
        if (request.getState() == null || request.getState().isEmpty()) {
            throw new ErrorNumException(400, VALIDATION_ERROR, "State is required for allowed actions.");
        }
        try {
            CLIParams params = toCliParams(request);
            params.stateForAllowedActions = request.getState();
            params.flowId = request.getFlowId();
            return responseOf(cliHelper.allowedActionsAsList(params));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public WorkflowInfoResponse<Map<String, Object>> toJson(WorkflowInfoRequest request) {
        validateXmlText(request);
        try {
            return responseOf(cliHelper.toMap(toCliParams(request)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public WorkflowInfoResponse<String> generateTestCases(WorkflowInfoRequest request) {
        validateXmlText(request);
        try {
            return responseOf(cliHelper.renderTestCases(toCliParams(request)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public WorkflowInfoResponse<String> visualizeTestCases(WorkflowInfoRequest request) {
        validateXmlText(request);
        try {
            return responseOf(cliHelper.renderTestPuml(toCliParams(request)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public WorkflowInfoResponse<Map<String, byte[]>> renderTestsAsStateDiagram(WorkflowInfoRequest request) {
        validateXmlText(request);
        try {
            return responseOf(PlantUmlToImageConverter.toPngMap(cliHelper.visualizeTestcaseAsStateDiagram(toCliParams(request))));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private CLIParams toCliParams(WorkflowInfoRequest request) {
        CLIParams params = new CLIParams();
        params.xmlText = request.getXmlText();
        params.stylingPropertiesText = request.getStylingPropertiesText();
        params.enablementPropertiesText = request.getEnablementPropertiesText();
        params.prefix = request.getPrefix();
        return params;
    }

    private void validateXmlText(WorkflowInfoRequest request) {
        if (request == null || request.getXmlText() == null || request.getXmlText().isEmpty()) {
            throw new ErrorNumException(400, VALIDATION_ERROR, "xmlText is required.");
        }
    }

    private <T> WorkflowInfoResponse<T> responseOf(T data) {
        return new WorkflowInfoResponse<>(data);
    }
}
