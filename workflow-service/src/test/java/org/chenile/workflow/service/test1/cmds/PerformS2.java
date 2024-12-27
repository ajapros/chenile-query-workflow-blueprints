package org.chenile.workflow.service.test1.cmds;

import org.chenile.stm.STMInternalTransitionInvoker;
import org.chenile.stm.State;
import org.chenile.stm.model.Transition;
import org.chenile.workflow.param.MinimalPayload;
import org.chenile.workflow.service.test1.mfg.MfgModel;
import org.chenile.workflow.service.test1.mfg.S2Payload;

public class PerformS2 extends PerformStep<S2Payload>{
    @Override
    public void transitionTo(MfgModel mfgModel, S2Payload payload, State startState, String eventId, State endState, STMInternalTransitionInvoker<?> stm, Transition transition) {
        super.transitionTo(mfgModel, payload, startState, eventId, endState, stm, transition);
        mfgModel.s2Strategy = payload.s2Strategy;
    }
}
