package org.chenile.workflow.service.stmcmds;

import org.chenile.stm.STMInternalTransitionInvoker;
import org.chenile.stm.State;
import org.chenile.stm.StateEntity;
import org.chenile.stm.action.STMTransitionAction;
import org.chenile.stm.model.Transition;

public abstract class AbstractSTMTransitionAction<StateEntityType extends StateEntity,PayloadType> implements STMTransitionAction<StateEntityType> {
    @SuppressWarnings("unchecked")
    @Override
    public void doTransition(StateEntityType stateEntity, Object transitionParam, State startState,
                             String eventId, State endState, STMInternalTransitionInvoker<?> stm, Transition transition) throws Exception {
        transitionTo(stateEntity, (PayloadType) transitionParam, startState,
                eventId, endState,  stm, transition);
    }

    public abstract void transitionTo(StateEntityType stateEntity,
              PayloadType transitionParam, State startState, String eventId, State endState,
              STMInternalTransitionInvoker<?> stm, Transition transition) throws Exception;

}
