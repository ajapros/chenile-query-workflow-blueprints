package org.chenile.workflow.service.test1.mfg;

import org.chenile.utils.entity.model.AbstractExtendedStateEntity;
import org.chenile.workflow.activities.model.ActivityEnabledStateEntity;
import org.chenile.workflow.activities.model.ActivityLog;

import java.util.*;

public class MfgModel extends AbstractExtendedStateEntity implements ActivityEnabledStateEntity {
    public Map<String,String> comments = new HashMap<>();
    // Capture if the model type is RETRO or MODERN.
    public String modelType;
    public Collection<ActivityLog> activities = new ArrayList<>();

    @Override
    public Collection<ActivityLog> obtainActivities() {
        return activities;
    }

    @Override
    public ActivityLog addActivity(String eventId,String comment) {
        ActivityLogImpl activityLog = new ActivityLogImpl();
        activityLog.success = true;
        activityLog.name = eventId;
        activityLog.comment = comment;
        activities.add(activityLog);
        return activityLog;
    }
}
