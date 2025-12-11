package org.chenile.workflow.service.stmcmds;

import org.chenile.base.exception.BadRequestException;
import org.chenile.owiz.BeanFactoryAdapter;
import org.chenile.owiz.Command;
import org.chenile.owiz.OrchExecutor;
import org.chenile.owiz.config.impl.XmlOrchConfigurator;
import org.chenile.owiz.impl.OrchExecutorImpl;
import org.chenile.stm.STMInternalTransitionInvoker;
import org.chenile.stm.State;
import org.chenile.stm.StateEntity;
import org.chenile.stm.action.STMTransitionAction;
import org.chenile.stm.model.Transition;
import org.chenile.workflow.activities.model.ActivityEnabledStateEntity;
import org.chenile.workflow.param.MinimalPayload;
import org.chenile.workflow.service.activities.ActivityChecker;
import org.chenile.workflow.service.stmcmds.dto.TransitionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.HashMap;
import java.util.Map;

/**
 * {@link org.chenile.stm.STM} supports a default transition action. If an event is not mapped to an
 * {@link STMTransitionAction} then the default transition action is invoked.
 * <p>This class is a good candidate to act as the default transition action. It provides the following
 * functionality:</p>
 * <p>It can provide a bridge to the OWIZ framework. If an OWIZ {@link Command} is configured or an
 * OWIZ {@link OrchExecutor} is configured in the metadata of the transition, then those are invoked.
 * It also supports if an OWIZ XML is specified. It instantiates the OrchExecutor based on the XML
 * and executes the OrchExecutor.</p>
 * <p>If an {@link ActivityChecker} is injected into this command, then it can log all MANDATORY
 * and OPTIONAL activities. It can also check that all activities are completed if the transition
 * is of type COMPLETION_CHECKER.</p>
 * <p>It can invoke a default transition command if injected with an {@link STMTransitionActionResolver}.
 * The resolver provides a component without it being specified as a componentName in the transition.</p>
 * @param <T> - the state entity
 */
public class BaseTransitionAction<T extends StateEntity> implements STMTransitionAction<T> {
	private STMTransitionActionResolver stmTransitionActionResolver = null;
	@Autowired
	private ApplicationContext applicationContext;
	/**
	 * This field needs to be set to enable activity management. Else it defaults to doing nothing.
	 */
	public ActivityChecker activityChecker;
	public BaseTransitionAction(){}
	public BaseTransitionAction(STMTransitionActionResolver stmTransitionActionResolver){
		this.stmTransitionActionResolver = stmTransitionActionResolver;
	}


	@Override
	public final void doTransition(T entity, Object transitionParam, State startState, String eventId, State endState,
			STMInternalTransitionInvoker<?> stm,Transition transition) throws Exception {
		Map<String, String> metadata = transition.getMetadata();
		TransitionContext<T> context = new TransitionContext<T>(entity, eventId, transitionParam,
				startState,endState,transition);
		if (metadata != null && metadata.get("command") != null) {
			@SuppressWarnings("unchecked")
			Command<TransitionContext<T>> command = (Command<TransitionContext<T>>)applicationContext.getBean(metadata.get("command"));
			command.execute(context);
			return;
		}else if (metadata != null &&metadata.get("orchExecutor") != null) {
			@SuppressWarnings("unchecked")
			OrchExecutor<TransitionContext<T>> command = (OrchExecutor<TransitionContext<T>>)applicationContext.getBean(metadata.get("orchExecutor"));
			command.execute(context);
			return;
		}else if(metadata != null &&metadata.get("orchestratedCommandsConfiguration") != null) {
			processMicroactions(metadata.get("orchestratedCommandsConfiguration"),context);
			return;
		}else {
			transition(entity,transitionParam,startState, eventId,endState, stm,transition);
			return;
		}
	}
				
	protected void processMicroactions(String microActionsXml, TransitionContext<T> context) throws Exception{
		OrchExecutor<TransitionContext<T>> orchExecutor = obtainOrchExecutor(microActionsXml);
		orchExecutor.execute(context);
	}
	
	protected Map<String,OrchExecutor<TransitionContext<T>>> orchMap = new HashMap<String, OrchExecutor<TransitionContext<T>>>();
	
	protected OrchExecutor<TransitionContext<T>> obtainOrchExecutor(String microActionsXml) {
		if (orchMap.get(microActionsXml) != null)
			return orchMap.get(microActionsXml);
		XmlOrchConfigurator<TransitionContext<T>> xmlOrchConfigurator = new XmlOrchConfigurator<TransitionContext<T>>();
		xmlOrchConfigurator.setBeanFactoryAdapter(new BeanFactoryAdapter() {
			@Override
			public Object lookup(String componentName) {
				return applicationContext.getBean(componentName);
			}
		});
		xmlOrchConfigurator.setFilename(microActionsXml);
		OrchExecutorImpl<TransitionContext<T>> orchExecutor = new OrchExecutorImpl<TransitionContext<T>>();
		orchExecutor.setOrchConfigurator(xmlOrchConfigurator);
		orchMap.put(microActionsXml, orchExecutor);
		return orchExecutor;
	}

	@SuppressWarnings("unchecked")
	public void transition(T entity, Object transitionParam, State startState,String eventId, State endState,
			STMInternalTransitionInvoker<?> stm, Transition transition) throws Exception {
		if(activityChecker != null) doActivityManagement(entity,transitionParam,startState,eventId,transition);
		if (stmTransitionActionResolver == null) return;
		STMTransitionAction<T> action = (STMTransitionAction<T>) stmTransitionActionResolver.getBean(eventId);
		if (action != null) {
			action.doTransition(entity,transitionParam,startState,eventId,endState,
					stm,transition);
		}
	}

	private void doActivityManagement(T entity, Object transitionParam, State startState, String eventId, Transition transition) {
		if (! (entity instanceof ActivityEnabledStateEntity aese)) return;
		if(activityChecker.isActivity(transition)){
			String comment = null;
			if (transitionParam instanceof MinimalPayload minimalPayload)
				comment = minimalPayload.getComment();
			aese.addActivity(eventId,comment);
		}
		if (activityChecker.isCompletionChecker(transition) &&
					!activityChecker.areAllActivitiesComplete(aese,startState))
			throw new BadRequestException(49000,
					"Transition " + eventId + " cannot be invoked without completing all the mandatory activities");
    }

}
