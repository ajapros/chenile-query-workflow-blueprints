package org.chenile.workflow.testcases;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import org.chenile.stm.EnablementStrategy;
import org.chenile.stm.State;
import org.chenile.stm.impl.ConfigBasedEnablementStrategy;
import org.chenile.stm.impl.ConfigProviderImpl;
import org.chenile.stm.impl.STMFlowStoreImpl;
import org.chenile.workflow.puml.PumlStyler;
import org.chenile.workflow.puml.STMPlantUmlSDGenerator;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.random.RandomGenerator;

public class STMTestCaseGenerator {
    STMFlowStoreImpl flowStore;
    TestcaseComputationStrategy testcaseComputationStrategy = null;
    Mustache testcaseVisualizer = null;
    STMPlantUmlSDGenerator plantUmlSDGenerator;
    public STMTestCaseGenerator(STMFlowStoreImpl flowStore){
        this.flowStore = flowStore;
        this.testcaseComputationStrategy = new TestcaseComputationStrategy(flowStore);
        this.plantUmlSDGenerator = new STMPlantUmlSDGenerator(this.flowStore);
    }

    private Mustache obtainTestcaseVisualizer(){
        if (testcaseVisualizer != null) return testcaseVisualizer;
        MustacheFactory mf = new DefaultMustacheFactory();
        testcaseVisualizer = mf.compile("testcases/testcases.mustache");
        return testcaseVisualizer;
    }
    public String visualizeTestcases() throws Exception{
        List<Testcase> model = buildFlow();
        Map<String,Object> map = new HashMap<>();
        map.put("testcases",model);
        StringWriter writer = new StringWriter();
        obtainTestcaseVisualizer().execute(writer, map).flush();
        return writer.toString();
    }

    public List<String> visualizeTestcasesWithStateDiagram() throws Exception {
        List<Testcase> testcases = buildFlow();
        List<String> ret = new ArrayList<>();
        for (Testcase testcase : testcases) {
            ret.add(visualizeTestcasesWithStateDiagram(testcase));
        }
        return ret;
    }

    private String visualizeTestcasesWithStateDiagram(Testcase testcase) throws Exception{
        PumlStyler.StyleElements elements = new PumlStyler.StyleElements();
        elements.thickness = 5;
        elements.color = randomColor();
        elements.lineStyle = "bold";
        PumlStyler.StyleRule styleRule = new PumlStyler.StyleRule();
        styleRule.expression = "testcase=="+ testcase.id;
        styleRule.elements = elements;
        plantUmlSDGenerator.transitionStyler.clear();
        plantUmlSDGenerator.transitionStyler.addRule(styleRule);
        EnablementStrategy enablementStrategy = getEnablementStrategy(testcase);
        flowStore.setEnablementStrategy(enablementStrategy);
        return plantUmlSDGenerator.toStateDiagram();
    }

    private static EnablementStrategy getEnablementStrategy(Testcase testcase) throws Exception {
        ConfigProviderImpl configProvider = new ConfigProviderImpl();
        for (TestcaseStep s: testcase.allSteps) {
            configProvider.setProperties("""
                    %s.%s.%s.meta.testcase=%s
                    %s.%s.meta.testcase=%s
                    %s.%s.meta.testcase=%s
                    """.formatted(s.transition.getFlowId(),
                    s.transition.getStateId(),s.transition.getEventId(), testcase.id,
                    s.transition.getFlowId(),s.transition.getStateId(), testcase.id,
                    s.transition.getNewFlowId(),s.transition.getNewStateId(), testcase.id));
        }
        return new ConfigBasedEnablementStrategy(configProvider);
    }

    // randomly choose from one of the following colors
    static final String[] colors = {"Fuchsia", "Teal", "MediumOrchid", "LightCoral", "Turquoise","Lime",
                    "DarkOrange", "IndianRed"};
    private String randomColor(){
        RandomGenerator randomGenerator = RandomGenerator.getDefault();
        int index = randomGenerator.nextInt(0, colors.length);
        return colors[index];
    }

    public String toTestCase() throws Exception{
        StringBuilder stringBuilder = new StringBuilder("[\n");
        for (Testcase testcase: buildFlow()) {
            if (!testcase.first) stringBuilder.append(",");
            stringBuilder.append("{\n");
            stringBuilder.append("\"first\": ").append(testcase.first).append(",\n");
            stringBuilder.append("\"id\": ").append(testcase.id).append(",\n");
            stringBuilder.append(printComments(testcase.comments));
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

    public static String printComments( List<String> comments) {
        StringBuilder stringBuilder = new StringBuilder();
        if (!comments.isEmpty()){
            boolean first = true;
            stringBuilder.append("\"comments\":[\n");
            for(String comment: comments){
                if (first)first = false;
                else stringBuilder.append(",");
                stringBuilder.append("\"").append(comment).append("\"");
            }
            stringBuilder.append("],\n");
        }
        return stringBuilder.toString();
    }


    public List<Testcase> buildFlow() throws Exception{
        State sd = flowStore.getInitialState(null);
        if(sd == null)return null;
        return testcaseComputationStrategy.toTestcases(sd);
    }

}
