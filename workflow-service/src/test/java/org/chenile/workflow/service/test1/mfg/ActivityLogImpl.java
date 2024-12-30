package org.chenile.workflow.service.test1.mfg;

import org.chenile.workflow.activities.model.ActivityLog;

public class ActivityLogImpl implements ActivityLog {
    public String name;
    public boolean success;
    public String comment;
    @Override
    public String name() {
        return name;
    }

    @Override
    public boolean success() {
        return success;
    }
}
