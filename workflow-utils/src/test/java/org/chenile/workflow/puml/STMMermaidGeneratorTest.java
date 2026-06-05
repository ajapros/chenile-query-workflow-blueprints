package org.chenile.workflow.puml;

import org.chenile.stm.dummy.DummyStore;
import org.chenile.stm.impl.STMFlowStoreImpl;
import org.chenile.stm.impl.XmlFlowReader;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class STMMermaidGeneratorTest {

    @Test
    void rendersAutomaticStatesAndTransitions() throws Exception {
        STMMermaidGenerator generator = new STMMermaidGenerator(loadStore("activity-with-auto-state.xml"));

        String diagram = generator.toStateDiagram();

        assertTrue(diagram.contains("stateDiagram-v2"));
        assertTrue(diagram.contains("[*] --> DEV"));
        assertTrue(diagram.contains("state IsDevComplete <<choice>>"));
        assertTrue(diagram.contains("DEV --> IsDevComplete: build"));
        assertTrue(diagram.contains("note right of IsDevComplete"));
    }

    @Test
    void rendersMainPathClasses() throws Exception {
        STMMermaidGenerator generator = new STMMermaidGenerator(loadStore("multiple-auto-states.xml"));

        String diagram = generator.toStateDiagram();

        assertTrue(diagram.contains("classDef mainPath"));
        assertTrue(diagram.contains("class CHECK_IF_FIRST_TIME mainPath"));
        assertTrue(diagram.contains("class CREDIT_CHECKS mainPath"));
        assertTrue(diagram.contains("class SUCCESSFUL mainPath"));
    }

    private STMFlowStoreImpl loadStore(String resourceName) throws Exception {
        STMFlowStoreImpl store = new DummyStore();
        XmlFlowReader flowReader = new XmlFlowReader(store);
        try (InputStream inputStream = getClass().getResourceAsStream("/org/chenile/workflow/testcases/" + resourceName)) {
            assertNotNull(inputStream);
            flowReader.parse(inputStream);
        }
        return store;
    }
}
