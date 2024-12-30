package org.chenile.workflow.service.activities;

import org.chenile.stm.State;
import org.chenile.stm.action.STMAutomaticStateComputation;
import org.chenile.workflow.activities.model.ActivityEnabledStateEntity;

public class AreActivitiesComplete implements STMAutomaticStateComputation<ActivityEnabledStateEntity> {
    public static final String YES = "yes";
    public static final String NO = "no";
    private final ActivityChecker activityChecker ;
    private final State whichState;

    public AreActivitiesComplete(ActivityChecker activityChecker, State whichState){
        this.activityChecker = activityChecker;
        this.whichState = whichState;
    }
    @Override
    public String execute(ActivityEnabledStateEntity stateEntity) throws Exception {
        return activityChecker.areAllActivitiesComplete(stateEntity,whichState)? YES : NO;
    }
}
