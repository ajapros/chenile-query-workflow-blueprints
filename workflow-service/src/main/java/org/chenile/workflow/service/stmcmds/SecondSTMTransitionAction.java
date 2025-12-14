package org.chenile.workflow.service.stmcmds;

import org.chenile.base.exception.ConfigurationException;
import org.chenile.stm.StateEntity;
import org.springframework.beans.factory.InitializingBean;

/**
 * <p>This class also supports the notion of a chain of transition actions that can be attached to
 *  the same event. The class registers itself to the main event transition action using
 *  {@link #registerAction(String, int)}. An index determines the order of registration. The transition
 *  actions are executed in the order of the index starting from the first transition action.</p>
 *  @param <StateEntityType> the subtype of the state entity
 *  @param <PayloadType> the type of payload
 */
@SuppressWarnings("unchecked")
public abstract class SecondSTMTransitionAction<StateEntityType extends StateEntity, PayloadType>
            extends AbstractSTMTransitionAction<StateEntityType,PayloadType>
            implements InitializingBean {

    final private STMTransitionActionResolver stmTransitionActionResolver ;
    final private String eventId;
    final private int index;

    protected SecondSTMTransitionAction(STMTransitionActionResolver stmTransitionActionResolver,
                                String eventId, int index) {
        this.stmTransitionActionResolver = stmTransitionActionResolver;
        this.eventId = eventId;
        this.index = index;
    }

    protected void registerAction(String eventId, int index){
        AbstractSTMTransitionAction<StateEntityType,PayloadType> action =
                (AbstractSTMTransitionAction<StateEntityType,PayloadType>) stmTransitionActionResolver.getBean(eventId);
        if (action == null)
            throw new ConfigurationException(5001,"No transition action found for event id " + eventId);
        if (index == 0)
            throw new ConfigurationException(5002,"Index 0 is not allowed");
        action.addCommand(index,this);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        registerAction(eventId,index);
    }
}
