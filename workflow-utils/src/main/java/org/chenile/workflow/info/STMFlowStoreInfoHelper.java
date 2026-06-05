package org.chenile.workflow.info;

import org.chenile.stm.State;
import org.chenile.stm.impl.STMActionsInfoProvider;
import org.chenile.stm.impl.STMFlowStoreImpl;
import org.chenile.workflow.puml.STMMermaidGenerator;
import org.chenile.workflow.puml.STMPlantUmlSDGenerator;
import org.chenile.workflow.testcases.STMTestCaseGenerator;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class STMFlowStoreInfoHelper {
    private final STMFlowStoreImpl stmFlowStore;
    private final STMActionsInfoProvider infoProvider;
    private final STMPlantUmlSDGenerator generator;
    private final STMTestCaseGenerator stmTestCaseGenerator;
    private final STMMermaidGenerator mermaidGenerator;

    public STMFlowStoreInfoHelper(STMFlowStoreImpl stmFlowStore, STMActionsInfoProvider infoProvider) {
        this.stmFlowStore = stmFlowStore;
        this.infoProvider = infoProvider;
        this.generator = new STMPlantUmlSDGenerator(stmFlowStore);
        this.stmTestCaseGenerator = new STMTestCaseGenerator(stmFlowStore);
        this.mermaidGenerator = new STMMermaidGenerator(stmFlowStore);
    }

    public List<String> allowedActions(String stateId, String flowId) {
        return this.infoProvider.getAllowedActions(makeState(stateId, flowId));
    }

    public String renderStateDiagram(String stylingPropertiesText) throws Exception {
        if (stylingPropertiesText != null && !stylingPropertiesText.isEmpty()) {
            loadStylingProperties(stylingPropertiesText);
        }
        return this.generator.toStateDiagram();
    }

    public String renderMermaidStateDiagram(String stylingPropertiesText) throws Exception {
        if (stylingPropertiesText != null && !stylingPropertiesText.isEmpty()) {
            loadStylingProperties(stylingPropertiesText);
        }
        return this.mermaidGenerator.toStateDiagram();
    }

    public Map<String,Object> toMap() {
        return this.stmFlowStore.toMap();
    }

    public String renderTestCases() throws Exception {
        return this.stmTestCaseGenerator.toTestCase();
    }

    public String renderTestPuml() throws Exception {
        return this.stmTestCaseGenerator.visualizeTestcases();
    }

    public Map<String,String> visualizeTestcaseAsStateDiagram() throws Exception {
        return this.stmTestCaseGenerator.visualizeTestcasesWithStateDiagram();
    }

    private State makeState(String stateId, String flowId) {
        String resolvedFlowId = (flowId == null || flowId.isEmpty()) ? this.stmFlowStore.getDefaultFlow() : flowId;
        return new State(stateId, resolvedFlowId);
    }

    private void loadStylingProperties(String stylingPropertiesText) throws Exception {
        try (InputStream inputStream = new ByteArrayInputStream(stylingPropertiesText.getBytes())) {
            this.generator.pumlStyler.load(inputStream);
        }
    }
}
