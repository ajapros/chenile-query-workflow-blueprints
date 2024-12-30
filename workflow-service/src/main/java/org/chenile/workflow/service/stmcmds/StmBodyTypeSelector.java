package org.chenile.workflow.service.stmcmds;

import com.fasterxml.jackson.core.type.TypeReference;
import org.chenile.core.context.ChenileExchange;
import org.chenile.owiz.Command;
import org.chenile.stm.impl.STMActionsInfoProvider;
import org.chenile.stm.model.EventInformation;
import org.chenile.workflow.service.impl.StateEntityServiceImpl;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

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
	private STMTransitionActionResolver stmTransitionActionResolver = null;
	private final STMActionsInfoProvider stmActionsInfoProvider;
	public StmBodyTypeSelector(STMActionsInfoProvider stmActionsInfoProvider) {
		this.stmActionsInfoProvider = stmActionsInfoProvider;
	}
	public StmBodyTypeSelector(STMActionsInfoProvider stmActionsInfoProvider,
							   STMTransitionActionResolver stmTransitionActionResolver) {
		this.stmActionsInfoProvider = stmActionsInfoProvider;
		this.stmTransitionActionResolver = stmTransitionActionResolver;
	}
	
	@Override
	public void execute(ChenileExchange exchange) throws Exception {
		String eventId = exchange.getHeader("eventID",String.class);
		EventInformation eventInformation = stmActionsInfoProvider.getEventInformation(eventId);
		if (null != eventInformation) {
			String bodyTypeClass = eventInformation.getMetadata().get("bodyType");
			Class<?> bodyType = Class.forName(bodyTypeClass);
			exchange.setBodyType(new TypeReference<Object>() {

				@Override
				public Type getType() {
					return bodyType;
				}
				
			});
		}else {
			checkIfPayloadTypeCanBeDerived(exchange,eventId);
		}
	}


	/**
	 *
	 * @param exchange - the exchange to which we need to set the TypeReference of the body type
	 * <p>This method works only if the eventID is the same as the name of the STMTransitionAction in
	 * the bean factory. It only works if the STMTransitionAction extends {@link AbstractSTMTransitionAction}.
	 * It computes the body type from the second argument of the {@link AbstractSTMTransitionAction#transitionTo} method that was
	 * overridden by the transition action.</p>
	 */
	private void checkIfPayloadTypeCanBeDerived(ChenileExchange exchange, String eventId) {
		if (stmTransitionActionResolver == null) return;
		try{
			Object bean = stmTransitionActionResolver.getBean(eventId);
			if (bean == null) return;
			if (!AbstractSTMTransitionAction.class.isAssignableFrom(bean.getClass()))return;
			Method[] methods = bean.getClass().getDeclaredMethods();
			for (Method m: methods){
				if (!m.getName().equals("transitionTo"))continue;
				if (m.isBridge()) continue;
				Type[] types = m.getGenericParameterTypes();
				exchange.setBodyType(new TypeReference<Object>() {
					@Override
					public Type getType() {
						return types[1];
					}
				});
				}

		}catch(Exception ignored){}
	}
}
