package org.chenile.workflow.service.store;

import org.chenile.core.context.ContextContainer;
import org.chenile.utils.entity.service.EntityStore;
import org.chenile.configuration.workflow.service.EntityStoreSelectionProperties;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.util.StringUtils;

import java.util.Optional;

/**
 * Resolves an {@link EntityStore} using the framework selection chain.
 */
final class TenantEntityStoreResolver {

    private final BeanFactory beanFactory;

    TenantEntityStoreResolver(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    EntityStore<?> resolve(String baseBeanName) {
        EntityStore<?> tenantStore = resolveByPrefix(ContextContainer.getInstance().getTenant(), baseBeanName);
        if (tenantStore != null) {
            return tenantStore;
        }

        for (String contextKey : selectionProperties().getContextKeys()) {
            String prefix = ContextContainer.getInstance().toMap().get(contextKey);
            EntityStore<?> contextStore = resolveByPrefix(prefix, baseBeanName);
            if (contextStore != null) {
                return contextStore;
            }
        }

        for (EntityStoreSelectionStrategy strategy : beanFactory
                .getBeanProvider(EntityStoreSelectionStrategy.class).orderedStream().toList()) {
            Optional<String> selectedBeanName = strategy.resolveStoreBeanName(baseBeanName);
            if (selectedBeanName == null || selectedBeanName.isEmpty()) {
                continue;
            }
            EntityStore<?> selectedStore = resolveBean(selectedBeanName.get(), baseBeanName);
            if (selectedStore != null) {
                return selectedStore;
            }
        }
        return null;
    }

    private EntityStoreSelectionProperties selectionProperties() {
        return beanFactory.getBeanProvider(EntityStoreSelectionProperties.class)
                .getIfAvailable(EntityStoreSelectionProperties::new);
    }

    private EntityStore<?> resolveByPrefix(String prefix, String baseBeanName) {
        if (!StringUtils.hasText(prefix)) {
            return null;
        }
        return resolveBean(prefix + StringUtils.capitalize(baseBeanName), baseBeanName);
    }

    private EntityStore<?> resolveBean(String beanName, String baseBeanName) {
        if (!StringUtils.hasText(beanName) || baseBeanName.equals(beanName) || !beanFactory.containsBean(beanName)) {
            return null;
        }
        Object bean = beanFactory.getBean(beanName);
        return bean instanceof EntityStore<?> entityStore ? entityStore : null;
    }
}
