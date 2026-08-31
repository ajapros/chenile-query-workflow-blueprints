package org.chenile.workflow.service.testprefix.issues.tenant0;

import org.chenile.workflow.service.testprefix.issues.IssueEntityStore;

/**
 * Test tenant store with a distinct id namespace, making store selection observable via REST.
 */
public class Tenant0IssueEntityStore extends IssueEntityStore {

    public Tenant0IssueEntityStore() {
        super("tenant0-");
    }
}
