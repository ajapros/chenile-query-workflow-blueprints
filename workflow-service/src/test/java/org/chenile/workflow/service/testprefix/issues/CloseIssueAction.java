package org.chenile.workflow.service.testprefix.issues;

import org.chenile.stm.STMInternalTransitionInvoker;
import org.chenile.stm.State;
import org.chenile.stm.action.STMTransitionAction;
import org.chenile.stm.model.Transition;
import org.chenile.workflow.param.MinimalPayload;

public class CloseIssueAction implements STMTransitionAction<Issue>{

	@Override
	public void doTransition(Issue issue, Object transitionParam, State startState, String eventId,
			State endState, STMInternalTransitionInvoker<?> stm, Transition transition) throws Exception {
		MinimalPayload payload = (MinimalPayload) transitionParam;
		issue.closeComment = payload.getComment();
	}

}
