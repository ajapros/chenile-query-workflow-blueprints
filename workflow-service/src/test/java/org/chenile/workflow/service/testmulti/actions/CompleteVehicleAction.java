package org.chenile.workflow.service.testmulti.actions;

import org.chenile.stm.STMInternalTransitionInvoker;
import org.chenile.stm.State;
import org.chenile.stm.model.Transition;
import org.chenile.workflow.param.MinimalPayload;
import org.chenile.workflow.service.stmcmds.AbstractSTMTransitionAction;
import org.chenile.workflow.service.testmulti.model.Vehicle;

public class CompleteVehicleAction extends AbstractSTMTransitionAction<Vehicle, MinimalPayload> {
	@Override
	public void transitionTo(Vehicle stateEntity, MinimalPayload transitionParam, State startState, String eventId,
			State endState, STMInternalTransitionInvoker<?> stm, Transition transition) {
		stateEntity.completionComment = transitionParam.getComment();
	}
}
