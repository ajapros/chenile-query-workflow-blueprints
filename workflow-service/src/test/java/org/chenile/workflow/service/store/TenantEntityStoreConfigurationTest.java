package org.chenile.workflow.service.store;

import org.chenile.configuration.workflow.service.TenantEntityStoreConfiguration;
import org.chenile.core.context.ContextContainer;
import org.chenile.utils.entity.service.EntityStore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TenantEntityStoreConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(EntityStoreTestConfiguration.class);

    @AfterEach
    void clearContext() {
        ContextContainer.getInstance().clear();
    }

    @Test
    void routesConcreteBaseStoreToTheTenantNamedStore() {
        contextRunner.run(context -> {
            BaseRecordingEntityStore baseStore = context.getBean("vehicleEntityStore", BaseRecordingEntityStore.class);
            TenantRecordingEntityStore tenantStore = context.getBean("tenant0VehicleEntityStore", TenantRecordingEntityStore.class);

            ContextContainer.getInstance().setTenant("tenant0");
            baseStore.store("tenant-value");

            assertEquals("tenant-value", tenantStore.retrieve("tenant-value"));
            assertNull(baseStore.valueFor("tenant-value"));
            assertEquals("tenant-value", baseStore.retrieve("tenant-value"));
        });
    }

    @Test
    void fallsBackToTheBaseStoreForUnknownAndMissingTenant() {
        contextRunner.run(context -> {
            BaseRecordingEntityStore baseStore = context.getBean("vehicleEntityStore", BaseRecordingEntityStore.class);

            ContextContainer.getInstance().setTenant("tenant1");
            baseStore.store("fallback-value");
            assertEquals("fallback-value", baseStore.retrieve("fallback-value"));

            ContextContainer.getInstance().clear();
            baseStore.store("base-value");
            assertEquals("base-value", baseStore.retrieve("base-value"));
        });
    }

    @Test
    void doesNotRouteNonEntityStoreOverloads() {
        contextRunner.run(context -> {
            BaseRecordingEntityStore baseStore = context.getBean("vehicleEntityStore", BaseRecordingEntityStore.class);

            ContextContainer.getInstance().setTenant("tenant0");

            assertEquals("helper-42", baseStore.retrieve(42L));
        });
    }

    @Test
    void routesMethodsDeclaredByAnEntityStoreSubinterface() {
        contextRunner.run(context -> {
            BaseRecordingEntityStore baseStore = context.getBean("vehicleEntityStore", BaseRecordingEntityStore.class);
            TenantRecordingEntityStore tenantStore = context.getBean("tenant0VehicleEntityStore", TenantRecordingEntityStore.class);

            ContextContainer.getInstance().setTenant("tenant0");
            baseStore.archive("tenant-archive");

            assertEquals("tenant-archive", tenantStore.archivedValueFor("tenant-archive"));
            assertNull(baseStore.archivedValueFor("tenant-archive"));
        });
    }

    @Test
    void fallsBackForExtendedOperationsWhenTheSelectedStoreDoesNotImplementTheirContract() {
        contextRunner.withPropertyValues("chenile.workflow.entity-store.context-keys=x-region")
                .run(context -> {
                    BaseRecordingEntityStore baseStore = context.getBean("vehicleEntityStore", BaseRecordingEntityStore.class);
                    RegionRecordingEntityStore regionStore = context.getBean("regionVehicleEntityStore", RegionRecordingEntityStore.class);

                    ContextContainer.getInstance().put("x-region", "region");
                    baseStore.archive("base-archive");

                    assertEquals("base-archive", baseStore.archivedValueFor("base-archive"));
                    assertNull(regionStore.valueFor("base-archive"));
                });
    }

    @Test
    void givesTenantStorePrecedenceOverContextAndCustomSelection() {
        contextRunner.withPropertyValues("chenile.workflow.entity-store.context-keys=x-region")
                .run(context -> {
                    BaseRecordingEntityStore baseStore = context.getBean("vehicleEntityStore", BaseRecordingEntityStore.class);
                    TenantRecordingEntityStore tenantStore = context.getBean("tenant0VehicleEntityStore", TenantRecordingEntityStore.class);
                    RegionRecordingEntityStore regionStore = context.getBean("regionVehicleEntityStore", RegionRecordingEntityStore.class);
                    CustomRecordingEntityStore customStore = context.getBean("customVehicleEntityStore", CustomRecordingEntityStore.class);

                    ContextContainer.getInstance().setTenant("tenant0");
                    ContextContainer.getInstance().put("x-region", "region");
                    ContextContainer.getInstance().put("x-chenile-apt", "custom");
                    baseStore.store("tenant-first");

                    assertEquals("tenant-first", tenantStore.valueFor("tenant-first"));
                    assertNull(regionStore.valueFor("tenant-first"));
                    assertNull(customStore.valueFor("tenant-first"));
                });
    }

    @Test
    void givesConfiguredContextKeysPrecedenceOverCustomSelection() {
        contextRunner.withPropertyValues("chenile.workflow.entity-store.context-keys=x-region")
                .run(context -> {
                    BaseRecordingEntityStore baseStore = context.getBean("vehicleEntityStore", BaseRecordingEntityStore.class);
                    RegionRecordingEntityStore regionStore = context.getBean("regionVehicleEntityStore", RegionRecordingEntityStore.class);
                    CustomRecordingEntityStore customStore = context.getBean("customVehicleEntityStore", CustomRecordingEntityStore.class);

                    ContextContainer.getInstance().put("x-region", "region");
                    ContextContainer.getInstance().put("x-chenile-apt", "custom");
                    baseStore.store("context-first");

                    assertEquals("context-first", regionStore.valueFor("context-first"));
                    assertNull(customStore.valueFor("context-first"));
                });
    }

    @Test
    void usesCustomSelectionBeforeFallingBackToTheBaseStore() {
        contextRunner.withPropertyValues("chenile.workflow.entity-store.context-keys=x-region")
                .run(context -> {
                    BaseRecordingEntityStore baseStore = context.getBean("vehicleEntityStore", BaseRecordingEntityStore.class);
                    CustomRecordingEntityStore customStore = context.getBean("customVehicleEntityStore", CustomRecordingEntityStore.class);

                    ContextContainer.getInstance().put("x-chenile-apt", "custom");
                    baseStore.store("custom-value");
                    assertEquals("custom-value", customStore.valueFor("custom-value"));

                    ContextContainer.getInstance().clear();
                    baseStore.store("base-value");
                    assertEquals("base-value", baseStore.valueFor("base-value"));
                });
    }

    @Test
    void ignoresASelectionCandidateThatIsNotAnEntityStore() {
        contextRunner.run(context -> {
            BaseRecordingEntityStore baseStore = context.getBean("vehicleEntityStore", BaseRecordingEntityStore.class);

            ContextContainer.getInstance().put("x-chenile-apt", "invalid");
            baseStore.store("fallback-after-invalid-selection");

            assertEquals("fallback-after-invalid-selection",
                    baseStore.retrieve("fallback-after-invalid-selection"));
        });
    }

    @Test
    void preservesExceptionsThrownByTheResolvedTenantStore() {
        contextRunner.run(context -> {
            BaseRecordingEntityStore baseStore = context.getBean("vehicleEntityStore", BaseRecordingEntityStore.class);
            ContextContainer.getInstance().setTenant("tenant0");

            IllegalStateException exception = assertThrows(IllegalStateException.class,
                    () -> baseStore.store("throw"));
            assertEquals("tenant-store failure", exception.getMessage());
        });
    }

    @Configuration(proxyBeanMethods = false)
    @Import({EntityStoreBeans.class, TenantEntityStoreConfiguration.class})
    static class EntityStoreTestConfiguration {
    }

    @Configuration(proxyBeanMethods = false)
    static class EntityStoreBeans {
        @Bean
        BaseRecordingEntityStore vehicleEntityStore() {
            return new BaseRecordingEntityStore();
        }

        @Bean
        TenantRecordingEntityStore tenant0VehicleEntityStore() {
            return new TenantRecordingEntityStore();
        }

        @Bean
        RegionRecordingEntityStore regionVehicleEntityStore() {
            return new RegionRecordingEntityStore();
        }

        @Bean
        CustomRecordingEntityStore customVehicleEntityStore() {
            return new CustomRecordingEntityStore();
        }

        @Bean
        String invalidVehicleEntityStore() {
            return "not-an-entity-store";
        }

        @Bean
        EntityStoreSelectionStrategy appTypeEntityStoreSelectionStrategy() {
            return baseBeanName -> {
                String appType = ContextContainer.getInstance().get("x-chenile-apt");
                if ("custom".equals(appType)) {
                    return Optional.of("custom" + StringUtils.capitalize(baseBeanName));
                }
                if ("invalid".equals(appType)) {
                    return Optional.of("invalid" + StringUtils.capitalize(baseBeanName));
                }
                return Optional.empty();
            };
        }
    }

    public interface RecordingEntityStore extends EntityStore<String> {
        void archive(String entity);
    }

    static class BaseRecordingEntityStore implements RecordingEntityStore {
        final Map<String, String> entries = new LinkedHashMap<>();
        final Map<String, String> archives = new LinkedHashMap<>();

        @Override
        public void store(String entity) {
            entries.put(entity, entity);
        }

        @Override
        public String retrieve(String id) {
            return entries.get(id);
        }

        @Override
        public void archive(String entity) {
            archives.put(entity, entity);
        }

        String retrieve(Long id) {
            return "helper-" + id;
        }

        String valueFor(String id) {
            return entries.get(id);
        }

        String archivedValueFor(String id) {
            return archives.get(id);
        }
    }

    static class TenantRecordingEntityStore implements RecordingEntityStore {
        final Map<String, String> entries = new LinkedHashMap<>();
        final Map<String, String> archives = new LinkedHashMap<>();

        @Override
        public void store(String entity) {
            if ("throw".equals(entity)) {
                throw new IllegalStateException("tenant-store failure");
            }
            entries.put(entity, entity);
        }

        @Override
        public String retrieve(String id) {
            return entries.get(id);
        }

        @Override
        public void archive(String entity) {
            archives.put(entity, entity);
        }

        String valueFor(String id) {
            return entries.get(id);
        }

        String archivedValueFor(String id) {
            return archives.get(id);
        }
    }

    static class RegionRecordingEntityStore implements EntityStore<String> {
        final Map<String, String> entries = new LinkedHashMap<>();

        @Override
        public void store(String entity) {
            entries.put(entity, entity);
        }

        @Override
        public String retrieve(String id) {
            return entries.get(id);
        }

        String valueFor(String id) {
            return entries.get(id);
        }
    }

    static class CustomRecordingEntityStore implements EntityStore<String> {
        final Map<String, String> entries = new LinkedHashMap<>();

        @Override
        public void store(String entity) {
            entries.put(entity, entity);
        }

        @Override
        public String retrieve(String id) {
            return entries.get(id);
        }

        String valueFor(String id) {
            return entries.get(id);
        }
    }
}
