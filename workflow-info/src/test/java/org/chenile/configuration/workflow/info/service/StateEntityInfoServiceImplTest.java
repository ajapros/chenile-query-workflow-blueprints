package org.chenile.configuration.workflow.info.service;

import org.chenile.stm.dummy.DummyStore;
import org.chenile.stm.impl.STMActionsInfoProvider;
import org.chenile.stm.impl.STMFlowStoreImpl;
import org.chenile.stm.impl.XmlFlowReader;
import org.chenile.workflow.info.model.StateEntityAllowedActionsRequest;
import org.chenile.workflow.info.model.StateEntityInfoRequest;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class StateEntityInfoServiceImplTest {
    @Test
    void runtimeBoundServiceUsesPreloadedFlowStore() throws Exception {
        StateEntityInfoServiceImpl service = new StateEntityInfoServiceImpl(loadStore(), loadInfoProvider());
        StateEntityInfoRequest request = new StateEntityInfoRequest();

        assertTrue(service.renderStateDiagram(request).getData().length > 0);
        assertFalse(service.toJson(request).getData().isEmpty());
        assertTrue(service.generateTestCases(request).getData().contains("\"steps\""));
        assertTrue(service.visualizeTestCases(request).getData().contains("assign"));
        assertFalse(service.renderTestsAsStateDiagram(request).getData().isEmpty());
        assertTrue(service.renderTestsAsStateDiagram(request).getData().values().iterator().next().length > 0);
    }

    @Test
    void runtimeBoundServiceReturnsAllowedActions() throws Exception {
        StateEntityInfoServiceImpl service = new StateEntityInfoServiceImpl(loadStore(), loadInfoProvider());
        StateEntityAllowedActionsRequest request = new StateEntityAllowedActionsRequest();
        request.setState("OPENED");

        assertEquals(1, service.allowedActions(request).getData().size());
        assertEquals("assign", service.allowedActions(request).getData().getFirst());
    }

    private STMFlowStoreImpl loadStore() throws Exception {
        STMFlowStoreImpl store = new DummyStore();
        XmlFlowReader flowReader = new XmlFlowReader(store);
        try (InputStream inputStream = new ByteArrayInputStream(loadWorkflowXml().getBytes(StandardCharsets.UTF_8))) {
            flowReader.parse(inputStream);
        }
        return store;
    }

    private STMActionsInfoProvider loadInfoProvider() throws Exception {
        return new STMActionsInfoProvider(loadStore());
    }

    private String loadWorkflowXml() throws Exception {
        try (InputStream inputStream = getClass().getResourceAsStream("/sample-workflow.xml")) {
            assertNotNull(inputStream);
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
