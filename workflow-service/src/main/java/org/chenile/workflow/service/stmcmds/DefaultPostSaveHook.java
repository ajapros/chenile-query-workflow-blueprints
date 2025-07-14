package org.chenile.workflow.service.stmcmds;

import org.chenile.stm.StateEntity;

public class DefaultPostSaveHook<StateEntityType extends StateEntity> implements PostSaveHook<StateEntityType>{
    private final STMTransitionActionResolver resolver;
    public DefaultPostSaveHook(STMTransitionActionResolver resolver){
        this.resolver = resolver;
    }
    @Override
    public void execute(StateEntityType entity, Object payload) {
        String stateId = entity.getCurrentState().getStateId();
        PostSaveHook<StateEntityType> postSaveHook =
                (PostSaveHook<StateEntityType>) resolver.resolvePostSaveHook(stateId);
        if(postSaveHook != null)  postSaveHook.execute(entity,payload);
    }
}
