package org.chenile.workflow.service.store;

import java.util.Optional;

/**
 * Application extension point for selecting an {@code EntityStore} bean.
 *
 * <p>Strategies run after tenant and configured context-key lookup. Return an empty optional to
 * let the next strategy, or ultimately the base store, handle the request.</p>
 */
public interface EntityStoreSelectionStrategy {

    /**
     * @param baseEntityStoreBeanName the bean name injected into the workflow service
     * @return a candidate {@code EntityStore} bean name, or empty when this strategy does not apply
     */
    Optional<String> resolveStoreBeanName(String baseEntityStoreBeanName);
}
