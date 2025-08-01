package org.chenile.workflow.service.impl;

import org.chenile.base.exception.ErrorNumException;
import org.chenile.base.exception.NotFoundException;
import org.chenile.stm.STM;
import org.chenile.stm.State;
import org.chenile.stm.StateEntity;
import org.chenile.stm.exception.STMException;
import org.chenile.stm.impl.STMActionsInfoProvider;
import org.chenile.utils.entity.service.EntityStore;
import org.chenile.workflow.api.StateEntityService;
import org.chenile.workflow.dto.StateEntityServiceResponse;

import java.util.List;
import java.util.Map;

/**
 * A generic implementation of the {@link StateEntityService} for all state entities. <br/>
 * This implementation uses the <a href="https://chenile.org/chenile-stm.html">Chenile State Machine</a><br/>
 * The state transition diagram should be customized for the particular entity and must be read
 * by the State Transition Machine that is injected into this service. After that, the machine
 * would entirely control the workflow. Please read the
 * <a href="https://chenile.org/chenile-workflow-tutorial.html">Chenile tutorial page</a>
 * about how to generate a project that uses this state machine.
 * @param <T>
 */
public class StateEntityServiceImpl<T extends StateEntity> implements StateEntityService<T> {
	private final STM<T> stm;
	protected final EntityStore<T> entityStore;
	private final STMActionsInfoProvider stmActionsInfoProvider;

	/**
	 *
	 * @param stm the state machine that has read the corresponding State Transition Diagram
	 * @param stmActionsInfoProvider the provider that gives out info about the state diagram
	 * @param entityStore the store for persisting the entity
	 */
	public StateEntityServiceImpl(STM<T> stm,STMActionsInfoProvider stmActionsInfoProvider,EntityStore<T> entityStore) {
		this.stm = stm;
		this.stmActionsInfoProvider = stmActionsInfoProvider;
		this.entityStore = entityStore;
	}

	/**
	 * Internally used by both the create and process methods.
	 * @param entity the entity which needs to be created or processed.
	 * @param event the event if it is not a create operation.
	 * @param payload the payload if the event field is not null
	 * @return the mutated entity along with allowed actions
	 */
	protected T processEntity(T entity, String event, Object payload)  {
		try {
			if (event == null)
				return stm.proceed(entity);
			else
				return stm.proceed(entity, event, payload);
		} catch (Exception e) {
			if (e instanceof STMException && 
					(((STMException)e).getMessageId() == STMException.INVALID_EVENTID ||
					((STMException)e).getMessageId() == STMException.INVALID_TRANSITION)) {
				throw new ErrorNumException(422, 6001,
						"Invalid event or transition: Error = " + e.getMessage());
			}else {
				if (e instanceof ErrorNumException ene) throw ene;
				e.printStackTrace();
				throw new ErrorNumException(500,6002,
						"Unknown happened in invoking event " + event + " in entity for current state = "
						+ entity.getCurrentState() + " . Error message = " + e.getMessage(),e);
			}
		}
	}

	/**
	 * An implementation of the create method that delegates to the entity store after filling up
	 * the mandatory attributes. It makes sure that it empties out the state if state has been
	 * supplied because we want STM to supply the initial state.<br/>
	 * @param entity the state entity that needs to be created.
	 * @return the entity with the allowed actions
	 */
	@Override
	public StateEntityServiceResponse<T> create(T entity) {
		entity.setCurrentState(null);
		T ret = processEntity(entity,null,null);
		return makeStateEntityResponse(ret);
	}

	/**
	 * An implementation of the process method that allows to track all the changes to the
	 * entity via events. This form is best suited for internal use and will not be exposed
	 * to the web. (We made the method public for some rare situations. This will be made
	 * protected over future releases)
	 * @param entity the entity on which the event has happened
	 * @param event - the name of the event that happened on the entity
	 * @param payload - Additional parameters for the event (event specific)
	 * @return the mutated entity along with allowed actions
	 */
	@Override
	public StateEntityServiceResponse<T> process(T entity, String event, Object payload) {
		T ret = processEntity(entity, event,payload);
		return makeStateEntityResponse(ret);
	}

	/**
	 * This form is expected from the web. The client merely must pass the ID and the event
	 * rather than the whole entity.
	 * @param id - the ID of the event. This is useful if the entire entity is not passed by the front end
	 * @param event - Name of the event that has happened on the entity
	 * @param payload - Additional parameters for the event (event specific)
	 * @return the mutated entity along with allowed actions
	 */
	@Override
	public StateEntityServiceResponse<T> processById(String id,  String event, Object payload) {
		T entity = entityStore.retrieve(id);
		if (entity == null) {
			throw new NotFoundException(6003, new Object[] {id});
		}
		T ret = processEntity(entity, event,payload);
		return makeStateEntityResponse(ret);
	}
	
	
//	@Override
	public List<Map<String, String>> getAllowedActionsAndMetadata(State state) {
		return stmActionsInfoProvider.getAllowedActionsAndMetadata(state);
	}
	
	@Override
	public List<Map<String, String>> getAllowedActionsAndMetadata(String id) {
		T entity = (T) entityStore.retrieve(id);
		if (entity == null) {
			throw new NotFoundException(6003, new Object[] {id});
		}
		return getAllowedActionsAndMetadata(entity.getCurrentState());
	}
	
	protected StateEntityServiceResponse<T> makeStateEntityResponse(T entity){
		StateEntityServiceResponse<T> sesr = new StateEntityServiceResponse<T>();
		sesr.setMutatedEntity(entity);
		State state = entity.getCurrentState();
		sesr.setAllowedActionsAndMetadata(getAllowedActionsAndMetadata(state));
		return sesr;
	}
	
	@Override
	public StateEntityServiceResponse<T> retrieve(String id) {
		T entity = (T) entityStore.retrieve(id);
		if (entity == null) {
			throw new NotFoundException(6003, new Object[] {id});
		}
		return makeStateEntityResponse(entity);
	}

	@Override
	public Map<String,Object> config(){
		return this.stmActionsInfoProvider.toMap();
	}
}
