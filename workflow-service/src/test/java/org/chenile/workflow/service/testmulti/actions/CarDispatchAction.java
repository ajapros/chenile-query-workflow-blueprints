package org.chenile.workflow.service.testmulti.actions;

import org.chenile.stm.STMInternalTransitionInvoker;
import org.chenile.stm.State;
import org.chenile.stm.model.Transition;
import org.chenile.workflow.service.stmcmds.AbstractSTMTransitionAction;
import org.chenile.workflow.service.testmulti.model.Car;
import org.chenile.workflow.service.testmulti.model.Vehicle;
import org.chenile.workflow.service.testmulti.payload.CarDispatchPayload;

public class CarDispatchAction extends AbstractSTMTransitionAction<Vehicle, CarDispatchPayload> {
	@Override
	public void transitionTo(Vehicle stateEntity, CarDispatchPayload transitionParam, State startState, String eventId,
			State endState, STMInternalTransitionInvoker<?> stm, Transition transition) {
		Car car = (Car) stateEntity;
		car.garageCode = transitionParam.garageCode;
		car.chargingSlot = transitionParam.chargingSlot;
		car.dispatchComment = transitionParam.getComment();
	}
}
