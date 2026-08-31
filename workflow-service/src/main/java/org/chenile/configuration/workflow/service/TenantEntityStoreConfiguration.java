package org.chenile.configuration.workflow.service;

import org.chenile.workflow.service.store.TenantEntityStoreBeanPostProcessor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registers tenant-specific {@code EntityStore} discovery for workflow applications.
 *
 * <p>Chenile applications conventionally scan {@code org.chenile.configuration}, so this
 * configuration is discovered with the rest of the framework configuration.</p>
 */
@Configuration
@EnableConfigurationProperties(EntityStoreSelectionProperties.class)
public class TenantEntityStoreConfiguration {

    @Bean
    static TenantEntityStoreBeanPostProcessor tenantEntityStoreBeanPostProcessor() {
        return new TenantEntityStoreBeanPostProcessor();
    }
}
