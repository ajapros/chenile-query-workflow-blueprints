package org.chenile.workflow.model;

import java.util.HashMap;

public class TransientMap extends HashMap<String,Object> {
    // contains the previous payload that was submitted to the STM.depends
    // This depends on the previous event that was invoked and hence
    // must be used with care.
    public Object previousPayload;

}
