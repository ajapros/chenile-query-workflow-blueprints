package org.chenile.workflow.activities.model;

import org.chenile.stm.StateEntity;

import java.util.Collection;

public interface ActivityEnabledStateEntity extends StateEntity {
    public Collection<ActivityLog> obtainActivities();
    public ActivityLog addActivity(String eventId,String comment);
}
