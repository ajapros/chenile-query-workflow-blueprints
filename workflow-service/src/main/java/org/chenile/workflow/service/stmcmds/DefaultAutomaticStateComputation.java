package org.chenile.workflow.service.stmcmds;

import org.chenile.stm.StateEntity;
import org.chenile.stm.action.STMAutomaticStateComputation;

public class DefaultAutomaticStateComputation<StateEntityType extends StateEntity> implements STMAutomaticStateComputation<StateEntityType> {
    private STMTransitionActionResolver resolver;
    public DefaultAutomaticStateComputation(STMTransitionActionResolver resolver){
        this.resolver = resolver;
    }

    @SuppressWarnings("unchecked")
    @Override
    public String execute(StateEntityType stateEntity) throws Exception {
        String stateId = stateEntity.getCurrentState().getStateId();
        STMAutomaticStateComputation<StateEntityType> automaticStateComputation =
                (STMAutomaticStateComputation<StateEntityType>) resolver.resolveAutomaticStateDescriptor(stateId);
        if(automaticStateComputation != null) return automaticStateComputation.execute(stateEntity);
        return "";
    }
}
