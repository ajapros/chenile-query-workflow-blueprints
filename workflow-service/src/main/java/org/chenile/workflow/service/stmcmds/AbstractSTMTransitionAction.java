package org.chenile.workflow.service.stmcmds;

import org.chenile.base.exception.ConfigurationException;
import org.chenile.stm.STMInternalTransitionInvoker;
import org.chenile.stm.State;
import org.chenile.stm.StateEntity;
import org.chenile.stm.action.STMTransitionAction;
import org.chenile.stm.model.Transition;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;
import java.util.TreeSet;

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
@SuppressWarnings("unchecked")
public abstract class AbstractSTMTransitionAction<StateEntityType extends StateEntity,PayloadType>
        implements STMTransitionAction<StateEntityType> {
    private final Set<OrderedCommand> cset = new TreeSet<>();
    @Autowired STMTransitionActionResolver stmTransitionActionResolver ;
    private boolean initiateChain = false;

     @Override
    public final void doTransition(StateEntityType stateEntity, Object transitionParam, State startState,
                             String eventId, State endState, STMInternalTransitionInvoker<?> stm, Transition transition) throws Exception {
        if (initiateChain){
            initExecuteChain(stateEntity, transitionParam, startState, eventId, endState, stm, transition);
        }else
            transitionTo(stateEntity, (PayloadType) transitionParam, startState,
                eventId, endState,  stm, transition);

    }

    public void addCommand(int index,AbstractSTMTransitionAction<StateEntityType,PayloadType> action){
        initiateChain = true;
        if (cset.isEmpty()){
            cset.add(new OrderedCommand(index,this));
        }
        cset.add(new OrderedCommand(index,this));
    }

    private void initExecuteChain(StateEntityType stateEntity, Object transitionParam, State startState,
                                  String eventId, State endState, STMInternalTransitionInvoker<?> stm, Transition transition) throws Exception {
        for (OrderedCommand oc: cset){
            oc.action.transitionTo(stateEntity,(PayloadType)transitionParam,startState,eventId,
                    endState,stm,transition);
        }
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

     public class OrderedCommand implements Comparable<OrderedCommand> {
         /**
          * This class is needed to ensure that the commands are retrieved in the correct order
          */
         public int index;
         public AbstractSTMTransitionAction<StateEntityType,PayloadType> action;
         public OrderedCommand(int index,AbstractSTMTransitionAction<StateEntityType,PayloadType> action){
             this.index = index;
             this.action = action;
         }

         public int compareTo(OrderedCommand o) {
             return (index - o.index );
         }
     }

     private void registerAction(String eventId, int index){
         AbstractSTMTransitionAction<StateEntityType,PayloadType> action =
                 (AbstractSTMTransitionAction<StateEntityType,PayloadType>) stmTransitionActionResolver.getBean(eventId);
         if (action == null)
             throw new ConfigurationException(5001,"No transition action found for event id " + eventId);
         if (index == 0)
             throw new ConfigurationException(5002,"Index 0 is not allowed");
         action.addCommand(index,this);
     }

}
