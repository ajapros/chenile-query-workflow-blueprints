package org.chenile.workflow.service.stmcmds;

import java.util.Date;

import org.chenile.stm.StateEntity;
import org.chenile.stm.action.STMAction;
import org.chenile.stm.impl.STMActionsInfoProvider;
import org.chenile.utils.entity.model.ExtendedStateEntity;
import org.chenile.utils.entity.service.EntityStore;
import org.chenile.workflow.service.impl.StateEntityHelper;

public class GenericEntryAction<T extends ExtendedStateEntity> implements STMAction<T>{

	private final EntityStore<T> entityStore;
	private final STMActionsInfoProvider stmActionsInfoProvider;
	private final PostSaveHook<T> postSaveHook;

	public  GenericEntryAction(EntityStore<T> entityStore, STMActionsInfoProvider stmActionsInfoProvider) {
		this.entityStore = entityStore;
		this.stmActionsInfoProvider = stmActionsInfoProvider;
		this.postSaveHook = null;
	}

	public  GenericEntryAction(EntityStore<T> entityStore, STMActionsInfoProvider stmActionsInfoProvider,PostSaveHook<T> postSaveHook) {
		this.entityStore = entityStore;
		this.stmActionsInfoProvider = stmActionsInfoProvider;
		this.postSaveHook = postSaveHook;
	}
	
	@Override
	public void execute(T entity) throws Exception {
		// update the workflow related attributes into the entity before storing it
		entity.setStateEntryTime(new Date());
		entity.setSlaLate(StateEntityHelper.getLateTimeInHours(stmActionsInfoProvider, entity.getCurrentState()));
		entity.setSlaTendingLate(StateEntityHelper.getGettingLateTimeInHours(stmActionsInfoProvider, entity.getCurrentState()));
		entityStore.store(entity);
		if (postSaveHook != null)postSaveHook.execute(entity,null);
	}


}
