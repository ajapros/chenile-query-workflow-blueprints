package org.chenile.workflow.info.model;

public class AllowedActionsRequest extends WorkflowInfoRequest {
    private String state;
    private String flowId;

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getFlowId() {
        return flowId;
    }

    public void setFlowId(String flowId) {
        this.flowId = flowId;
    }
}
