package org.chenile.workflow.service.testprefix.issues.custom;

import org.chenile.workflow.service.testprefix.issues.IssueEntityStore;

public class CustomIssueEntityStore extends IssueEntityStore {

    public CustomIssueEntityStore() {
        super("custom-");
    }
}
