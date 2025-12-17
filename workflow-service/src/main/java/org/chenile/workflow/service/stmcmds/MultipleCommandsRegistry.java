package org.chenile.workflow.service.stmcmds;

import org.chenile.stm.STMInternalTransitionInvoker;
import org.chenile.stm.State;
import org.chenile.stm.StateEntity;
import org.chenile.stm.model.Transition;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class MultipleCommandsRegistry<StateEntityType extends StateEntity,PayloadType> {
    private final Map<String,Set<OrderedCommand>> registry = new HashMap<>();
    protected void addCommand(String eventId,int index,SecondSTMTransitionAction<StateEntityType,PayloadType> action){
        Set<OrderedCommand> cset = registry.computeIfAbsent(eventId,e -> new TreeSet<>());
        cset.add(new OrderedCommand(index,action));
        registry.put(eventId,cset);
    }

    public void initExecuteChain(StateEntityType stateEntity, Object transitionParam, State startState,
                                  String eventId, State endState, STMInternalTransitionInvoker<?> stm, Transition transition) throws Exception {
        Set<OrderedCommand> cset = registry.get(eventId);
        if (cset ==  null) return;
        for (OrderedCommand oc: cset){
            oc.action.transitionTo(stateEntity,(PayloadType)transitionParam,startState,eventId,
                    endState,stm,transition);
        }
    }
    public class OrderedCommand implements Comparable<OrderedCommand> {
        /**
         * This class is needed to ensure that the commands are retrieved in the correct order
         */
        public int index;
        public SecondSTMTransitionAction<StateEntityType,PayloadType> action;
        public OrderedCommand(int index,SecondSTMTransitionAction<StateEntityType,PayloadType> action){
            this.index = index;
            this.action = action;
        }

        public int compareTo(OrderedCommand o) {
            return (index - o.index );
        }
    }
}
