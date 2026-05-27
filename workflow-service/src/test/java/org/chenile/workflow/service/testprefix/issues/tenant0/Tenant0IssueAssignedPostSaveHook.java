package org.chenile.workflow.service.testprefix.issues.tenant0;

import org.chenile.stm.State;
import org.chenile.workflow.model.TransientMap;
import org.chenile.workflow.service.stmcmds.PostSaveHook;
import org.chenile.workflow.service.testprefix.issues.Issue;

public class Tenant0IssueAssignedPostSaveHook implements PostSaveHook<Issue> {
	@Override
	public void execute(State startState, State endState, Issue entity, TransientMap payload) {
		entity.postSaveHookMarker = "tenant0-issue-assigned";
	}
}
