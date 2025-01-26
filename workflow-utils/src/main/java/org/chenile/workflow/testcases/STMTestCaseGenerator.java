package org.chenile.workflow.testcases;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import org.chenile.stm.State;
import org.chenile.stm.impl.STMFlowStoreImpl;
import org.chenile.stm.model.StateDescriptor;
import org.chenile.stm.model.Transition;

import java.io.StringWriter;
import java.util.*;

public class STMTestCaseGenerator {
    STMFlowStoreImpl flowStore;
    TestcaseComputationStrategy testcaseComputationStrategy = null;
    Mustache testcaseVisualizer = null;
    public STMTestCaseGenerator(STMFlowStoreImpl flowStore){
        this.flowStore = flowStore;
        this.testcaseComputationStrategy = new TestcaseComputationStrategy(flowStore);
    }

    private Mustache obtainTestcaseVisualizer(){
        if (testcaseVisualizer != null) return testcaseVisualizer;
        MustacheFactory mf = new DefaultMustacheFactory();
        testcaseVisualizer = mf.compile("testcases/testcases.mustache");
        return testcaseVisualizer;
    }
    public String visualizeTestcase() throws Exception{
        List<Testcase> model = buildFlow();
        StringWriter writer = new StringWriter();
        obtainTestcaseVisualizer().execute(writer, model).flush();
        return writer.toString();
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
        return testcaseComputationStrategy.toTestcases(sd);
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
