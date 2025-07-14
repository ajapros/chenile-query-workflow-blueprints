package org.chenile.workflow.service.stmcmds;

import org.chenile.stm.StateEntity;

public interface PostSaveHook<StateEntityType extends StateEntity> {
    public void execute(StateEntityType entity, Object payload);
}
