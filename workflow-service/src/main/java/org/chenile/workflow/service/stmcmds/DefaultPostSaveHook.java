package org.chenile.workflow.service.stmcmds;

import org.chenile.stm.State;
import org.chenile.stm.StateEntity;
import org.chenile.workflow.model.TransientMap;

public class DefaultPostSaveHook<StateEntityType extends StateEntity> implements PostSaveHook<StateEntityType>{
    private final STMTransitionActionResolver resolver;
    public DefaultPostSaveHook(STMTransitionActionResolver resolver){
        this.resolver = resolver;
    }

    @Override
    public void execute(State startState, State endState, StateEntityType entity, TransientMap payload) {
        String stateId = entity.getCurrentState().getStateId();
        PostSaveHook<StateEntityType> postSaveHook =
                (PostSaveHook<StateEntityType>) resolver.resolvePostSaveHook(stateId);
        if(postSaveHook != null)  postSaveHook.execute(startState,endState,entity,payload);
    }
}
