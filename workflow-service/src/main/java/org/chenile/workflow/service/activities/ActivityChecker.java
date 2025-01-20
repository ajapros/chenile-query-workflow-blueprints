package org.chenile.workflow.service.activities;

import org.chenile.stm.STMFlowStore;
import org.chenile.stm.State;
import org.chenile.stm.model.StateDescriptor;
import org.chenile.stm.model.Transition;
import org.chenile.workflow.activities.model.ActivityEnabledStateEntity;
import org.chenile.workflow.activities.model.ActivityLog;
import org.chenile.workflow.activities.model.ActivityType;

import java.util.Map;

/**
 * Given that there are a bunch of activities that can be accomplished in a State and that
 * all these activities are tracked by the state entity, this strategy checks if all these
 * activities are completed. In the state machine, activities are modelled as events. They
 * are annotated with a metadata called activity whose value is set to MANDATORY or OPTIONAL.
 * <p>An event with no activity metadata is not an activity by definition. </p>
 * All mandatory activities need to be completed.
 * Optional activities will not be checked for completion.
 */
public class ActivityChecker {
    private static final String ACTIVITY_DATA_NAME = "activity";
    private final STMFlowStore stmFlowStore;
    public ActivityChecker(STMFlowStore stmFlowStore){
       this.stmFlowStore = stmFlowStore;
    }

    /**
     *
     * @param stateEntity the activity enabled state entity.
     * @return false if we find even a single MANDATORY activity (in the current state) that has not been marked SUCCESSFUL.
     */
    public boolean areAllActivitiesComplete(ActivityEnabledStateEntity stateEntity){
        return areAllActivitiesComplete(stateEntity,stateEntity.getCurrentState());
    }
    /**
     *
     * @param stateEntity the activity enabled state entity.
     * @param whichState the state against which all activities will be tracked.
     * @return false if we find even a single MANDATORY activity that has not been marked SUCCESSFUL.
     */
    public boolean areAllActivitiesComplete(ActivityEnabledStateEntity stateEntity,
                                            State whichState){
        StateDescriptor sd = stmFlowStore.getStateInfo(whichState);
        Map<String, Transition> transitions = sd.getTransitions();
        for (Transition transition: transitions.values()){
            Map<String,String> metadata = transition.getMetadata();
            ActivityType type = getActivityType(metadata);
            if(type == ActivityType.NONE || type == ActivityType.COMPLETION_CHECKER) continue;
            if (type == ActivityType.MANDATORY &&
                    !isActivityComplete(transition.getEventId(), stateEntity)){
                return false;
            }
        }
        return true;
    }

    /**
     *
     * @param eventId the event ID to check if it is complete
     * @param stateEntity the activity enabled state entity to check.
     * @return true if we found at least one activity that matches the event ID and is marked successful
     */
    private boolean isActivityComplete(String eventId,
                   ActivityEnabledStateEntity stateEntity) {
        for(ActivityLog activityLog: stateEntity.obtainActivities()){
            if (activityLog.getName().equals(eventId))
                return activityLog.getSuccess();
        }
        return false;
    }

    private ActivityType getActivityType(Map<String,String> metadata) {
        String activity = metadata.get(ACTIVITY_DATA_NAME);
        if (activity == null) return ActivityType.NONE;
        if (activity.equals(ActivityType.MANDATORY.name())) return ActivityType.MANDATORY;
        else if (activity.equals(ActivityType.OPTIONAL.name())) return ActivityType.OPTIONAL;
        else if (activity.equals(ActivityType.COMPLETION_CHECKER.name())) return ActivityType.COMPLETION_CHECKER;
        return ActivityType.NONE;
    }

    public boolean isMandatoryActivity(ActivityEnabledStateEntity stateEntity,
                                       String eventId){
        State state = stateEntity.getCurrentState();
        StateDescriptor sd = stmFlowStore.getStateInfo(state);
        Transition transition = sd.getTransitions().get(eventId);
        return isMandatoryActivity(transition);
    }
    public boolean isMandatoryActivity(Transition transition){
        return getActivityType(transition.getMetadata()) == ActivityType.MANDATORY;
    }
    public boolean isCompletionChecker(ActivityEnabledStateEntity stateEntity,
                                       String eventId){
        State state = stateEntity.getCurrentState();
        StateDescriptor sd = stmFlowStore.getStateInfo(state);
        Transition transition = sd.getTransitions().get(eventId);
        return isCompletionChecker(transition);
    }
    public boolean isCompletionChecker(Transition transition){
        return getActivityType(transition.getMetadata()) == ActivityType.COMPLETION_CHECKER;
    }

    public boolean isOptionalActivity(ActivityEnabledStateEntity stateEntity,
                                       String eventId){
        State state = stateEntity.getCurrentState();
        StateDescriptor sd = stmFlowStore.getStateInfo(state);
        Transition transition = sd.getTransitions().get(eventId);
        return isOptionalActivity(transition);
    }

    public boolean isOptionalActivity(Transition transition){
        return getActivityType(transition.getMetadata()) == ActivityType.OPTIONAL;
    }
    public boolean isActivity(ActivityEnabledStateEntity stateEntity,
                                       String eventId){
        State state = stateEntity.getCurrentState();
        StateDescriptor sd = stmFlowStore.getStateInfo(state);
        Transition transition = sd.getTransitions().get(eventId);
        return isActivity(transition);
    }
    public boolean isActivity(Transition transition){
        ActivityType activityType =  getActivityType(transition.getMetadata());
        return (activityType != ActivityType.NONE && activityType != ActivityType.COMPLETION_CHECKER);
    }

}
