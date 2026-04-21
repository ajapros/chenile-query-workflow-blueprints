package org.chenile.workflow.service.testmulti.actions;

import org.chenile.stm.STMInternalTransitionInvoker;
import org.chenile.stm.State;
import org.chenile.stm.model.Transition;
import org.chenile.workflow.service.stmcmds.AbstractSTMTransitionAction;
import org.chenile.workflow.service.testmulti.model.Bus;
import org.chenile.workflow.service.testmulti.model.Vehicle;
import org.chenile.workflow.service.testmulti.payload.BusDispatchPayload;

public class BusDispatchAction extends AbstractSTMTransitionAction<Vehicle, BusDispatchPayload> {
	@Override
	public void transitionTo(Vehicle stateEntity, BusDispatchPayload transitionParam, State startState, String eventId,
			State endState, STMInternalTransitionInvoker<?> stm, Transition transition) {
		Bus bus = (Bus) stateEntity;
		bus.routeCode = transitionParam.routeCode;
		bus.seatCapacity = transitionParam.seatCapacity;
		bus.dispatchComment = transitionParam.getComment();
	}
}
