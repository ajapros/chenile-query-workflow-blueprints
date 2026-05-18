package org.chenile.configuration.workflow.info.service;

import org.chenile.workflow.cli.CLIHelper;
import org.chenile.workflow.info.model.AllowedActionsRequest;
import org.chenile.workflow.info.model.WorkflowInfoRequest;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class WorkflowInfoServiceImplTest {
    private final WorkflowInfoServiceImpl service = new WorkflowInfoServiceImpl(new CLIHelper());

    @Test
    void xmlDrivenServiceEmitsWorkflowInformation() throws Exception {
        WorkflowInfoRequest request = new WorkflowInfoRequest();
        request.setXmlText(loadWorkflowXml());

        assertTrue(service.renderStateDiagram(request).getData().startsWith("@startuml"));
        assertFalse(service.toJson(request).getData().isEmpty());
        assertTrue(service.generateTestCases(request).getData().contains("\"steps\""));
        assertTrue(service.visualizeTestCases(request).getData().contains("assign"));
        assertFalse(service.renderTestsAsStateDiagram(request).getData().isEmpty());
    }

    @Test
    void allowedActionsUsesTypedResponse() throws Exception {
        AllowedActionsRequest request = new AllowedActionsRequest();
        request.setXmlText(loadWorkflowXml());
        request.setState("OPENED");

        assertEquals(1, service.allowedActions(request).getData().size());
        assertEquals("assign", service.allowedActions(request).getData().getFirst());
    }

    private String loadWorkflowXml() throws Exception {
        try (InputStream inputStream = getClass().getResourceAsStream("/sample-workflow.xml")) {
            assertNotNull(inputStream);
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
