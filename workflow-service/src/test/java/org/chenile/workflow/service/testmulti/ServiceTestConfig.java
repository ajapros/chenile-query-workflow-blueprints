package org.chenile.workflow.service.testmulti;

import org.chenile.stm.STM;
import org.chenile.stm.action.STMTransitionAction;
import org.chenile.stm.impl.BeanFactoryAdapter;
import org.chenile.stm.impl.STMActionsInfoProvider;
import org.chenile.stm.impl.STMFlowStoreImpl;
import org.chenile.stm.impl.STMImpl;
import org.chenile.stm.impl.XmlFlowReader;
import org.chenile.stm.spring.SpringBeanFactoryAdapter;
import org.chenile.workflow.service.impl.StateEntityServiceImpl;
import org.chenile.workflow.service.stmcmds.BaseTransitionAction;
import org.chenile.workflow.service.stmcmds.GenericEntryAction;
import org.chenile.workflow.service.stmcmds.GenericExitAction;
import org.chenile.workflow.service.stmcmds.STMTransitionActionResolver;
import org.chenile.workflow.service.stmcmds.StmBodyTypeSelector;
import org.chenile.workflow.service.testmulti.actions.BusDispatchAction;
import org.chenile.workflow.service.testmulti.actions.CarDispatchAction;
import org.chenile.workflow.service.testmulti.actions.CompleteVehicleAction;
import org.chenile.workflow.service.testmulti.controller.VehicleController;
import org.chenile.workflow.service.testmulti.model.Vehicle;
import org.chenile.workflow.service.testmulti.repo.VehicleRepository;
import org.chenile.workflow.service.testmulti.store.VehicleEntityStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;

@Configuration
@PropertySource("classpath:org/chenile/workflow/service/testmulti/TestWorkflowService.properties")
@SpringBootApplication(scanBasePackages = { "org.chenile.configuration", "org.chenile.workflow.service.testmulti" })
@EnableJpaRepositories(basePackageClasses = VehicleRepository.class)
@ActiveProfiles("unittest")
public class ServiceTestConfig extends SpringBootServletInitializer {
	private static final String FLOW_DEFINITION_FILE = "org/chenile/workflow/service/testmulti/vehicles.xml";

	@Bean
	BeanFactoryAdapter vehicleBeanFactoryAdapter() {
		return new SpringBeanFactoryAdapter();
	}

	@Bean
	STMFlowStoreImpl vehicleFlowStore(@Qualifier("vehicleBeanFactoryAdapter") BeanFactoryAdapter vehicleBeanFactoryAdapter) {
		STMFlowStoreImpl stmFlowStore = new STMFlowStoreImpl();
		stmFlowStore.setBeanFactory(vehicleBeanFactoryAdapter);
		return stmFlowStore;
	}

	@Bean
	XmlFlowReader vehicleFlowReader(@Qualifier("vehicleFlowStore") STMFlowStoreImpl flowStore) throws Exception {
		XmlFlowReader flowReader = new XmlFlowReader(flowStore);
		flowReader.setFilename(FLOW_DEFINITION_FILE);
		return flowReader;
	}

	@Bean
	STM<Vehicle> vehicleEntityStm(@Qualifier("vehicleFlowStore") STMFlowStoreImpl stmFlowStore) {
		STMImpl<Vehicle> stm = new STMImpl<>();
		stm.setStmFlowStore(stmFlowStore);
		return stm;
	}

	@Bean
	STMActionsInfoProvider vehicleActionsInfoProvider(@Qualifier("vehicleFlowStore") STMFlowStoreImpl stmFlowStore) {
		return new STMActionsInfoProvider(stmFlowStore);
	}

	@Bean
	ApplicationRunner vehicleSchemaInitializer(DataSource dataSource) {
		return args -> new ResourceDatabasePopulator(
				new ClassPathResource("org/chenile/workflow/service/testmulti/schema.sql"))
				.execute(dataSource);
	}

	@Bean
	VehicleEntityStore vehicleEntityStore(VehicleRepository vehicleRepository) {
		return new VehicleEntityStore(vehicleRepository);
	}

	@Bean
	StateEntityServiceImpl<Vehicle> _vehicleStateEntityService_(
			@Qualifier("vehicleEntityStm") STM<Vehicle> stm,
			@Qualifier("vehicleActionsInfoProvider") STMActionsInfoProvider vehicleInfoProvider,
			@Qualifier("vehicleEntityStore") VehicleEntityStore entityStore) {
		return new StateEntityServiceImpl<>(stm, vehicleInfoProvider, entityStore);
	}

	@Bean
	GenericEntryAction<Vehicle> vehicleEntryAction(
			@Qualifier("vehicleEntityStore") VehicleEntityStore entityStore,
			@Qualifier("vehicleActionsInfoProvider") STMActionsInfoProvider vehicleInfoProvider) {
		return new GenericEntryAction<>(entityStore, vehicleInfoProvider);
	}

	@Bean
	GenericExitAction<Vehicle> vehicleExitAction() {
		return new GenericExitAction<>();
	}

	@Bean
	STMTransitionAction<Vehicle> defaultVehicleTransitionAction() {
		return new BaseTransitionAction<>();
	}

	@Bean
	STMTransitionActionResolver vehicleTransitionActionResolver(
			@Qualifier("defaultVehicleTransitionAction") STMTransitionAction<Vehicle> defaultVehicleTransitionAction) {
		return new STMTransitionActionResolver("vehicle", defaultVehicleTransitionAction);
	}

	@Bean
	StmBodyTypeSelector vehicleBodyTypeSelector(
			@Qualifier("vehicleActionsInfoProvider") STMActionsInfoProvider vehicleInfoProvider,
			@Qualifier("vehicleTransitionActionResolver") STMTransitionActionResolver vehicleTransitionActionResolver,
			@Qualifier("vehicleEntityStore") VehicleEntityStore vehicleEntityStore) {
		return new StmBodyTypeSelector(vehicleInfoProvider, vehicleTransitionActionResolver, vehicleEntityStore);
	}

	@Bean
	STMTransitionAction<Vehicle> vehicleSTMTransitionAction(
			@Qualifier("vehicleTransitionActionResolver") STMTransitionActionResolver vehicleTransitionActionResolver) {
		return new BaseTransitionAction<>(vehicleTransitionActionResolver);
	}

	@Bean
	BusDispatchAction vehicleBusDispatch() {
		return new BusDispatchAction();
	}

	@Bean
	CarDispatchAction vehicleCarDispatch() {
		return new CarDispatchAction();
	}

	@Bean
	CompleteVehicleAction vehicleComplete() {
		return new CompleteVehicleAction();
	}

}
