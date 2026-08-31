package org.chenile.configuration.workflow.service;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration for context-based {@code EntityStore} selection.
 */
@ConfigurationProperties(prefix = "chenile.workflow.entity-store")
public class EntityStoreSelectionProperties {

    /**
     * ContextContainer keys consulted, in order, after tenant lookup and before application strategies.
     */
    private List<String> contextKeys = new ArrayList<>();

    public List<String> getContextKeys() {
        return contextKeys;
    }

    public void setContextKeys(List<String> contextKeys) {
        this.contextKeys = contextKeys == null ? new ArrayList<>() : new ArrayList<>(contextKeys);
    }
}
