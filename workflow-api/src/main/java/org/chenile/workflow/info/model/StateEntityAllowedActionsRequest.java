package org.chenile.workflow.info.model;

public class StateEntityAllowedActionsRequest extends StateEntityInfoRequest {
    private String state;

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
