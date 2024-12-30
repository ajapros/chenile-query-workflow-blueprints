package org.chenile.workflow.service.test1.cmds;

import org.chenile.stm.STMInternalTransitionInvoker;
import org.chenile.stm.State;
import org.chenile.stm.model.Transition;
import org.chenile.workflow.service.test1.mfg.MfgModel;
import org.chenile.workflow.service.test1.mfg.FinishManufacturingPayload;

public class FinishManufacturing extends PerformStep<FinishManufacturingPayload>{
    @Override
    public void transitionTo(MfgModel mfgModel, FinishManufacturingPayload payload, State startState, String eventId, State endState, STMInternalTransitionInvoker<?> stm, Transition transition) {
        super.transitionTo(mfgModel, payload, startState, eventId, endState, stm, transition);
        mfgModel.modelType = payload.modelType;
    }
}
