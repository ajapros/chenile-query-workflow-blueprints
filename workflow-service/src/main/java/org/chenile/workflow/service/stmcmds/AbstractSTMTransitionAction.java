package org.chenile.workflow.service.stmcmds;

import org.chenile.stm.STMInternalTransitionInvoker;
import org.chenile.stm.State;
import org.chenile.stm.StateEntity;
import org.chenile.stm.action.STMTransitionAction;
import org.chenile.stm.model.Transition;

/**
 * {@link STMTransitionAction} uses Object as a payload type. Hence it is
 * impossible to infer the precise Payload type from the implementation of STMTransition Action. This class
 * serves as a bridge for sub-classes to enforce a Paylaod Type. By extending this class, sub-classes
 * declare the precise Payload Type that they expect.
 * <p>Without this class. Payload type had to be declared in the STM configuration. The blue print
 * for STM assumes that all STM Transition Actions extend this class.</p>
 * @param <StateEntityType> the sub-type of the state entity
 * @param <PayloadType> the type of payload
 */
public abstract class AbstractSTMTransitionAction<StateEntityType extends StateEntity,PayloadType> implements STMTransitionAction<StateEntityType> {
    @SuppressWarnings("unchecked")

    @Override
    public final void doTransition(StateEntityType stateEntity, Object transitionParam, State startState,
                             String eventId, State endState, STMInternalTransitionInvoker<?> stm, Transition transition) throws Exception {
        transitionTo(stateEntity, (PayloadType) transitionParam, startState,
                eventId, endState,  stm, transition);
    }

    /**
     * Implement this method to start using your expected payload type.
     * @param stateEntity the state entity that has been passed to STM
     * @param transitionParam The transition param with the correct payload type
     * @param startState state at which the event occurred
     * @param eventId the event that happened
     * @param endState the end state
     * @param stm this is for invoking other STMs
     * @param transition the definition of the entire transition in the STM configuration
     * @throws Exception if an exception occurs.
     */
    public abstract void transitionTo(StateEntityType stateEntity,
              PayloadType transitionParam, State startState, String eventId, State endState,
              STMInternalTransitionInvoker<?> stm, Transition transition) throws Exception;

}
