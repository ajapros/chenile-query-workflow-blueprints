package org.chenile.workflow.service.activities;

import org.chenile.stm.State;
import org.chenile.stm.action.scriptsupport.BaseCustomComponentPropertiesAction;
import org.chenile.workflow.activities.model.ActivityEnabledStateEntity;

public class AreActivitiesComplete extends BaseCustomComponentPropertiesAction<ActivityEnabledStateEntity> {
    public static final String YES = "yes";
    public static final String NO = "no";
    private final ActivityChecker activityChecker ;
    private State whichState = null;

    public AreActivitiesComplete(ActivityChecker activityChecker){
        this.activityChecker = activityChecker;
    }
    public AreActivitiesComplete(ActivityChecker activityChecker, State whichState){
        this.activityChecker = activityChecker;
        this.whichState = whichState;
    }
    @Override
    public String doExecute(ActivityEnabledStateEntity stateEntity) throws Exception {
        return activityChecker.areAllActivitiesComplete(stateEntity,getStateToUse(stateEntity))? YES : NO;
    }

    protected State getStateToUse(ActivityEnabledStateEntity stateEntity){
        if (whichState != null) return whichState;
        String flowId = getComponentProperty(stateEntity,"whichFlowId",
                stateEntity.getCurrentState().getFlowId());
        String stateId = getComponentProperty(stateEntity,"whichStateId");
        if (flowId == null || stateId == null) return null;
        return new State(stateId,flowId);
    }
}
