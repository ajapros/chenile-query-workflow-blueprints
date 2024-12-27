package org.chenile.workflow.service.stmcmds;

import org.chenile.stm.action.STMTransitionAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

/**
 * Resolves bean name for the STM transition action in spring using the event ID and a prefix which
 * is specific to the workflow. Obtains the bean from the Spring Bean factory.
 * <p>This strategy is shared between the {@link BaseTransitionAction} and the {@link StmBodyTypeSelector} </p>
 * <p> If the {@link #defaultSTMTransitionAction} is set then that is returned in case a custom
 * event transition action is not available.</p>
 */
public class STMTransitionActionResolver {
    private String prefix = "";
    @Autowired private ApplicationContext applicationContext;
    private STMTransitionAction<?> defaultSTMTransitionAction;
    public STMTransitionActionResolver(String prefix){
        this.prefix = prefix;
    }
    public STMTransitionActionResolver(String prefix, STMTransitionAction<?> stmTransitionAction){
        this.prefix = prefix;
        this.defaultSTMTransitionAction = stmTransitionAction;
    }

    private  String beanName(String eventId){
        if (prefix.isEmpty()) return eventId;
        return prefix + eventId.substring(0,1).toUpperCase() + eventId.substring(1);
    }

    public STMTransitionAction<?> getBean(String eventId) {
        try {
            return (STMTransitionAction<?>) applicationContext.getBean(beanName(eventId));
        }catch(Exception e){
             return defaultSTMTransitionAction;
        }
    }
}
