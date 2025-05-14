package org.chenile.workflow.service.testprefix.issues.tenant0;

import org.chenile.stm.STMInternalTransitionInvoker;
import org.chenile.stm.State;
import org.chenile.stm.action.STMTransitionAction;
import org.chenile.stm.model.Transition;
import org.chenile.workflow.service.testprefix.issues.AssignIssuePayload;
import org.chenile.workflow.service.testprefix.issues.Issue;

public class Tenant0AssignIssueAction implements STMTransitionAction<Issue>{

	@Override
	public void doTransition(Issue issue, Object transitionParam, State startState, String eventId,
			State endState, STMInternalTransitionInvoker<?> stm, Transition transition) throws Exception {
		AssignIssuePayload payload = (AssignIssuePayload) transitionParam;
		issue.assignee = "tenant0";
		issue.assignComment = payload.getComment();
	}

}
