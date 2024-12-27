package org.chenile.workflow.service.test1.mfg;

import java.util.HashMap;
import java.util.Map;

import org.chenile.utils.entity.service.EntityStore;

public class MfgEntityStore implements EntityStore<MfgModel>{
	private final Map<String, MfgModel> theStore = new HashMap<>();
	public static int counter = 1;
	@Override
	public void store(MfgModel entity) {
		if (entity.getId() == null) {
			entity.setId(counter++ + "");
		}
		theStore.put(entity.getId(), entity);		
	}

	@Override
	public MfgModel retrieve(String id) {
		return theStore.get(id);
	}

}
