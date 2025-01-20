package org.chenile.workflow.service.test1.cmds;

import org.chenile.stm.STMInternalTransitionInvoker;
import org.chenile.stm.State;
import org.chenile.stm.model.Transition;
import org.chenile.workflow.param.MinimalPayload;
import org.chenile.workflow.service.activities.ActivityChecker;
import org.chenile.workflow.service.stmcmds.AbstractSTMTransitionAction;
import org.chenile.workflow.service.test1.mfg.MfgModel;
import org.springframework.beans.factory.annotation.Autowired;

public class PerformStep<PayloadType extends MinimalPayload> extends AbstractSTMTransitionAction<MfgModel, PayloadType> {
    @Autowired
    ActivityChecker activityChecker;
    @Override
    public void transitionTo(MfgModel mfgModel, PayloadType payload,
                 State startState, String eventId, State endState, STMInternalTransitionInvoker<?> stm,
                 Transition transition) {
        if (!activityChecker.isActivity(mfgModel,eventId))
            mfgModel.comments.put(eventId,payload.getComment());
    }
}
