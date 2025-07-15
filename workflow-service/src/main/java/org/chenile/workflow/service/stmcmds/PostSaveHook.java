package org.chenile.workflow.service.stmcmds;

import org.chenile.stm.StateEntity;
import org.chenile.workflow.model.TransientMap;

public interface PostSaveHook<StateEntityType extends StateEntity> {
    public void execute(StateEntityType entity, TransientMap payload);
}
