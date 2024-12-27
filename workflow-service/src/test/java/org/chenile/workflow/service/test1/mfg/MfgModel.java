package org.chenile.workflow.service.test1.mfg;

import org.chenile.stm.State;
import org.chenile.utils.entity.model.AbstractExtendedStateEntity;

import java.util.HashMap;
import java.util.Map;

public class MfgModel extends AbstractExtendedStateEntity {
    public Map<String,String> comments = new HashMap<>();
    // record specific information about S2. In this case we are recording some specific strategy
    // the S2 transition action needs to capture and update the strategy
    public String s2Strategy;
}
