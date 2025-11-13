package org.chenile.workflow.service.stmcmds;

import org.chenile.stm.State;
import org.chenile.stm.StateEntity;
import org.chenile.workflow.model.TransientMap;

public interface PostSaveHook<StateEntityType extends StateEntity> {
    public void execute(State startState, State endState, StateEntityType entity, TransientMap payload);
}
