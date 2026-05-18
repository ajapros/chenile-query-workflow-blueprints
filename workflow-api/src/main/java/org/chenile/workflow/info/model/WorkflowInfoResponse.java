package org.chenile.workflow.info.model;

public class WorkflowInfoResponse<T> {
    private T data;

    public WorkflowInfoResponse() {
    }

    public WorkflowInfoResponse(T data) {
        this.data = data;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
