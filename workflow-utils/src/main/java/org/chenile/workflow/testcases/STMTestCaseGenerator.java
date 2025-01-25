package org.chenile.workflow.testcases;

import org.chenile.stm.State;
import org.chenile.stm.impl.STMFlowStoreImpl;
import org.chenile.stm.model.StateDescriptor;
import org.chenile.stm.model.Transition;

import java.util.*;

public class STMTestCaseGenerator {
    STMFlowStoreImpl flowStore;
    TestcasePaths testcasePaths = null;
    public STMTestCaseGenerator(STMFlowStoreImpl flowStore){
        this.flowStore = flowStore;
        this.testcasePaths = new TestcasePaths(flowStore);
    }

    public String toTestCase() throws Exception{
        StringBuilder stringBuilder = new StringBuilder("[\n");
        for (Testcase testcase: buildFlow()) {
            if (!testcase.first) stringBuilder.append(",");
            stringBuilder.append("{\n");
            stringBuilder.append("\"first\": ").append(testcase.first).append(",\n");
            stringBuilder.append("\"steps\": [\n");
            for (TestcaseStep t: testcase.steps){
                if (!t.first) stringBuilder.append(",");
                stringBuilder.append(t);
            }
            stringBuilder.append("]\n");
            stringBuilder.append("}\n");
        }
        return stringBuilder.append("]\n").toString();
    }

    public List<Testcase> buildFlow() throws Exception{
        State sd = flowStore.getInitialState(null);
        if(sd == null)return null;
        return testcasePaths.toTestcases(sd);
    }

    private StateDescriptor getInitialStateDescriptor(){
        for (StateDescriptor sd: flowStore.getAllStates()){
            if (sd.isInitialState()){
                return sd;
            }
        }
        return null;
    }
    private boolean isInMainFlow(StateDescriptor sd){
        return isInMainFlow(sd.getMetadata());
    }

    private boolean isInMainFlow(Transition t){
        return isInMainFlow(t.getMetadata());
    }

    private boolean isInMainFlow(Map<String, String> metadata) {
        String val = metadata.get("mainFlow");
        return Boolean.parseBoolean(val);
    }
}
