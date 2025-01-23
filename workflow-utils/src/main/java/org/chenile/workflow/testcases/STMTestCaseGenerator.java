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
        boolean firstTestcase = true;
        for (Deque<Testcase> stack: buildFlow()) {
            if (!firstTestcase) stringBuilder.append(",");
            else firstTestcase = false;
            boolean first = true;
            stringBuilder.append("[\n");
            for (Testcase t: stack){
                if (!first) stringBuilder.append(",");
                else first = false;
                stringBuilder.append("""
                        {
                            "from": "%s",
                            "event": "%s",
                            "to": "%s"
                        }
                        """.formatted(t.from,
                        t.event, t.to));
            }
            stringBuilder.append("]\n");
        }
        return stringBuilder.append("]\n").toString();
    }

    public List<Deque<Testcase>> buildFlow() throws Exception{
        State sd = flowStore.getInitialState(null);
        if(sd == null)return null;
        Set<State> visitedStates = new HashSet<>();
        return testcasePaths.cachedComputePaths(sd,visitedStates);
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
