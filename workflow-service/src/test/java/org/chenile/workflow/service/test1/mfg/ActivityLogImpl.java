package org.chenile.workflow.service.test1.mfg;

import org.chenile.workflow.activities.model.ActivityLog;

public class ActivityLogImpl implements ActivityLog {
    public String name;
    public boolean success;
    public String comment;
    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean getSuccess() {
        return success;
    }

    @Override
    public String getComment() {
        return comment;
    }
}
