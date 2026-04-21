package org.chenile.workflow.service.stmcmds;

import org.chenile.core.context.ChenileExchange;
import org.chenile.owiz.Command;
import org.chenile.stm.StateEntity;
import org.chenile.stm.impl.STMActionsInfoProvider;
import org.chenile.stm.model.EventInformation;
import org.chenile.utils.entity.service.EntityStore;
import org.chenile.workflow.service.impl.StateEntityServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.core.type.TypeReference;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Selects the body type of the {@link ChenileExchange} based on the event Id passed
 * This should be used as the body type selector for the {@link StateEntityServiceImpl#process(org.chenile.stm.StateEntity, String, Object)}
 * method. Will work with all subclasses.
 * Assumptions:<br/>
 * <ol>
 * <li>Second parameter for the process method will be mapped to a header parameter called "eventId".
 * <li>The body type has been defined in the stm in a states/event-information section. eventId must be mapped to
 * meta attribute called "meta-bodyType"
 * </ol>
 * <p>
 * @author Raja Shankar Kolluru
 *
 */
	public class StmBodyTypeSelector implements Command<ChenileExchange>{
		private static final Logger logger = LoggerFactory.getLogger(StmBodyTypeSelector.class);
		private STMTransitionActionResolver stmTransitionActionResolver = null;
		private final STMActionsInfoProvider stmActionsInfoProvider;
		private EntityStore<? extends StateEntity> entityStore = null;

		public record EventData(String description, TypeReference<?> typeReference){}
		private  Map<String, EventData> configs = null;

	public StmBodyTypeSelector(STMActionsInfoProvider stmActionsInfoProvider) {
		this.stmActionsInfoProvider = stmActionsInfoProvider;
	}
	public StmBodyTypeSelector(STMActionsInfoProvider stmActionsInfoProvider,
							   STMTransitionActionResolver stmTransitionActionResolver) {
		this.stmActionsInfoProvider = stmActionsInfoProvider;
		this.stmTransitionActionResolver = stmTransitionActionResolver;
	}

	public StmBodyTypeSelector(STMActionsInfoProvider stmActionsInfoProvider,
							   STMTransitionActionResolver stmTransitionActionResolver,
							   EntityStore<? extends StateEntity> entityStore) {
		this.stmActionsInfoProvider = stmActionsInfoProvider;
		this.stmTransitionActionResolver = stmTransitionActionResolver;
		this.entityStore = entityStore;
	}

		public void storeBodyTypeSelector(){
			if (configs == null) configs = new LinkedHashMap<>();

			stmActionsInfoProvider.getStmFlowStore().getAllFlows().forEach(e->{
				e.getStates().forEach((k,v) -> {
						v.getTransitions().forEach((key, t) -> {
                            TypeReference<?> payload = checkIfPayloadTypeCanBeDerived(key);
                            if (payload != null) {
                                EventData eventData = new EventData(t.getMetadata().get("description"), payload);
                                configs.put(key, eventData);
                            }
                        });
	            });
			});
		}

	@Override
	public void execute(ChenileExchange exchange) throws Exception {
		String eventId = exchange.getHeader("eventID",String.class);
		StateEntity entity = resolveEntity(exchange);
		TypeReference<?> typeReference = getPayloadBodyType(eventId, entity);
		if (null != typeReference) {
			exchange.setBodyType(typeReference);
		}
	}

	public  TypeReference<?> getPayloadBodyType(String eventId) throws ClassNotFoundException {
		return getPayloadBodyType(eventId, null);
	}

	public  TypeReference<?> getPayloadBodyType(String eventId, StateEntity entity) throws ClassNotFoundException {
		TypeReference<?> derivedType = checkIfPayloadTypeCanBeDerived(eventId, entity);
		if (derivedType != null) return derivedType;
		EventInformation eventInformation = stmActionsInfoProvider.getEventInformation(eventId);
		if (null != eventInformation &&
				eventInformation.getMetadata().get("bodyType")!=null) {
			String bodyTypeClass = (String)eventInformation.getMetadata().get("bodyType");
			Class<?> bodyType = Class.forName(bodyTypeClass);
			return new TypeReference<>() {

				@Override
				public Type getType() {
					return bodyType;
				}

			};
		}
		return checkIfPayloadTypeCanBeDerived(eventId, null);
	}


	/**
	 *
	 * This method works only if the eventID is the same as the name of the STMTransitionAction in
	 * the bean factory. It only works if the STMTransitionAction extends {@link AbstractSTMTransitionAction}.
	 * It computes the body type from the second argument of the {@link AbstractSTMTransitionAction#transitionTo} method that was
	 * overridden by the transition action.</p>
	 */
	private TypeReference<?> checkIfPayloadTypeCanBeDerived(String eventId, StateEntity entity) {
		if (stmTransitionActionResolver == null) return null;
		try{
			Object bean = stmTransitionActionResolver.getBean(eventId, entity);
			if (bean == null) return null;
			if (!AbstractSTMTransitionAction.class.isAssignableFrom(bean.getClass()))return null;
			Method[] methods = bean.getClass().getDeclaredMethods();
			for (Method m: methods){
				if (!m.getName().equals("transitionTo"))continue;
				if (m.isBridge()) continue;
				Type[] types = m.getGenericParameterTypes();
				return new TypeReference<Object>() {
					@Override
					public Type getType() {
						return types[1];
					}
				};
			}

		}catch(Exception e){
			logger.warn("Unable to derive payload type for eventId {}", eventId, e);
		}
		return null;
	}

	private TypeReference<?> checkIfPayloadTypeCanBeDerived(String eventId) {
		return checkIfPayloadTypeCanBeDerived(eventId, null);
	}

	private StateEntity resolveEntity(ChenileExchange exchange) {
		if (entityStore == null) return null;
		String id = exchange.getHeader("id", String.class);
		if (id == null) return null;
		try {
			return entityStore.retrieve(id);
		} catch (Exception e) {
			logger.warn("Unable to resolve entity for id {}", id, e);
			return null;
		}
	}

	public Map<String, EventData> getConfigs() {
		if (configs == null) {
			storeBodyTypeSelector();
		}
		return configs;
	}
}
