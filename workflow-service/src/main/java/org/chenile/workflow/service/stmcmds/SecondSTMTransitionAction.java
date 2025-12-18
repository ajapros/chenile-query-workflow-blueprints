package org.chenile.workflow.service.stmcmds;

import org.chenile.base.exception.ConfigurationException;
import org.chenile.stm.StateEntity;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

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
            extends AbstractSTMTransitionAction<StateEntityType,PayloadType> {

    final private MultipleCommandsRegistry<StateEntityType,PayloadType> multipleCommandsRegistry ;
    final private String[] eventIds;
    final private int index;

    protected SecondSTMTransitionAction(MultipleCommandsRegistry<StateEntityType
            ,PayloadType> commandsRegistry, int index, String... eventIds) {
        this.multipleCommandsRegistry = commandsRegistry;
        this.eventIds = eventIds;
        this.index = index;
    }

    protected void registerAction(String eventId, int index){
        if (index == 0)
            throw new ConfigurationException(5002,"Index 0 is not allowed");
        multipleCommandsRegistry.addCommand(eventId,index,this);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void init() throws Exception {
        for (String eventId: eventIds)
            registerAction(eventId,index);
    }
}
