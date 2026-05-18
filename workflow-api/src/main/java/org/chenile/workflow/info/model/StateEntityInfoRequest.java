package org.chenile.workflow.info.model;

public class StateEntityInfoRequest {
    private String stylingPropertiesText;
    private String flowId;

    public String getStylingPropertiesText() {
        return stylingPropertiesText;
    }

    public void setStylingPropertiesText(String stylingPropertiesText) {
        this.stylingPropertiesText = stylingPropertiesText;
    }

    public String getFlowId() {
        return flowId;
    }

    public void setFlowId(String flowId) {
        this.flowId = flowId;
    }
}
