package org.chenile.workflow.service.test1;


import org.chenile.stm.*;
import org.chenile.stm.action.STMTransitionAction;
import org.chenile.stm.impl.*;
import org.chenile.stm.model.Transition;
import org.chenile.stm.spring.SpringBeanFactoryAdapter;
import org.chenile.workflow.param.MinimalPayload;
import org.chenile.workflow.service.activities.ActivityChecker;
import org.chenile.workflow.service.activities.AreActivitiesComplete;
import org.chenile.workflow.service.impl.StateEntityServiceImpl;
import org.chenile.workflow.service.stmcmds.*;
import org.chenile.workflow.service.test1.cmds.FinishManufacturing;
import org.chenile.workflow.service.test1.cmds.PerformStep;
import org.chenile.workflow.service.test1.mfg.FinishManufacturingPayload;
import org.chenile.workflow.service.test1.mfg.MfgEntityStore;
import org.chenile.workflow.service.test1.mfg.MfgModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ActiveProfiles;

@Configuration
@PropertySource("classpath:org/chenile/workflow/service/test1/TestWorkflowService.properties")
@SpringBootApplication(scanBasePackages = { "org.chenile.configuration" , "org.chenile.workflow.service.test1"})
@ActiveProfiles("unittest")
public class ServiceTestConfig extends SpringBootServletInitializer{

	private static final String FLOW_DEFINITION_FILE = "org/chenile/workflow/service/test1/mfg.xml";
	@Bean BeanFactoryAdapter mfgBeanFactoryAdapter() {
		return new SpringBeanFactoryAdapter();
	}
	
	@Bean STMFlowStoreImpl mfgFlowStore(@Qualifier("mfgBeanFactoryAdapter") BeanFactoryAdapter mfgBeanFactoryAdapter) {
		STMFlowStoreImpl stmFlowStore = new STMFlowStoreImpl();
		stmFlowStore.setBeanFactory(mfgBeanFactoryAdapter);
		return stmFlowStore;
	}
	
	@Bean XmlFlowReader mfgFlowReader(@Qualifier("mfgFlowStore") STMFlowStoreImpl flowStore) throws Exception{
		XmlFlowReader flowReader = new XmlFlowReader(flowStore);
        flowReader.setFilename(FLOW_DEFINITION_FILE);
		return flowReader;
	}
	
	@Bean @Autowired STM<MfgModel> mfgEntityStm(@Qualifier("mfgFlowStore") STMFlowStoreImpl stmFlowStore) {
		STMImpl<MfgModel> stm = new STMImpl<>();
		stm.setStmFlowStore(stmFlowStore);
		return stm;
	}
	
	@Bean @Autowired STMActionsInfoProvider mfgActionsInfoProvider(@Qualifier("mfgFlowStore") STMFlowStoreImpl stmFlowStore) {
		return new STMActionsInfoProvider(stmFlowStore);
	}
	
	@Bean
	MfgEntityStore mfgEntityStore() {
		return new MfgEntityStore();
	}
	
	@Bean @Autowired StateEntityServiceImpl<MfgModel> _mfgStateEntityService_(
			@Qualifier("mfgEntityStm") STM<MfgModel> stm,
			@Qualifier("mfgActionsInfoProvider") STMActionsInfoProvider issueInfoProvider,
			@Qualifier("mfgEntityStore") MfgEntityStore entityStore){
		return new StateEntityServiceImpl<>(stm, issueInfoProvider, entityStore);
	}
	
	// Now we start constructing the STM Components 
	
	@Bean @Autowired GenericEntryAction<MfgModel> mfgEntryAction(@Qualifier("mfgEntityStore") MfgEntityStore entityStore,
			@Qualifier("mfgActionsInfoProvider") STMActionsInfoProvider mfgInfoProvider){
		return new GenericEntryAction<>(entityStore, mfgInfoProvider);
	}
	
	@Bean GenericExitAction<MfgModel> mfgExitAction(){
		return new GenericExitAction<>();
	}

	@Bean STMTransitionAction<MfgModel> defaultSTMTransitionAction() {
		return new PerformStep<MinimalPayload>();
	}

	@Bean
	STMTransitionActionResolver stmTransitionActionResolver(
			@Qualifier("defaultSTMTransitionAction") STMTransitionAction<MfgModel> defaultSTMTransitionAction){
		return new STMTransitionActionResolver("mfg",defaultSTMTransitionAction);
	}
	@Bean @Autowired StmBodyTypeSelector mfgBodyTypeSelector(
		@Qualifier("mfgActionsInfoProvider") STMActionsInfoProvider issueInfoProvider,
		@Qualifier("stmTransitionActionResolver") STMTransitionActionResolver stmTransitionActionResolver) {
		return new StmBodyTypeSelector(issueInfoProvider,stmTransitionActionResolver);
	}

	@Bean STMTransitionAction<MfgModel> mfgBaseTransitionAction(
		@Qualifier("stmTransitionActionResolver") STMTransitionActionResolver stmTransitionActionResolver,
		@Qualifier("mfgActivitiesChecker") ActivityChecker activityChecker ){
		BaseTransitionAction<MfgModel> bta = new BaseTransitionAction<>(stmTransitionActionResolver);
		bta.activityChecker = activityChecker;
		return bta;
	}

	@Bean ActivityChecker mfgActivitiesChecker(@Qualifier("mfgFlowStore") STMFlowStore stmFlowStore){
		return new ActivityChecker(stmFlowStore);
	}

	@Bean
	AreActivitiesComplete areOutOfAssemblyLineActivitiesComplete(@Qualifier("mfgActivitiesChecker") ActivityChecker activityChecker){
		return new AreActivitiesComplete(activityChecker, new State("OUT_OF_ASSEMBLY_LINE", "MFG_FLOW"));
	}

	@Bean AreActivitiesComplete activitiesCompletionCheck(@Qualifier("mfgActivitiesChecker") ActivityChecker activityChecker) {
		return new AreActivitiesComplete(activityChecker);
	}
	@Bean ConfigProviderImpl mfgConfigProvider() {
		return new ConfigProviderImpl();
	}

	@Bean ConfigBasedEnablementStrategy mfgConfigBasedEnablementStrategy(
			@Qualifier("mfgConfigProvider") ConfigProvider configProvider) {
		return new ConfigBasedEnablementStrategy(configProvider);
	}


	// Create the specific transition actions here. To automatically wire them into the STM
	// use the convention of "mfg" + eventId for the method name. (mfg is the prefix passed to the
	// TransitionActionResolver above.) This will ensure that these are detected automatically by the
	// Workflow system. The payload types will be detected as well so that there is no need to
	// introduce an <event-information/> segment in the mfg.xml
	@Bean STMTransitionAction<MfgModel> mfgFinishManufacturing() {
		return new FinishManufacturing();
	}




	@Bean
	SecondSTMTransitionAction<MfgModel, FinishManufacturingPayload> secondSTMTransitionAction(@Qualifier("stmTransitionActionResolver")
														STMTransitionActionResolver stmTransitionActionResolver
																){
		return new SecondSTMTransitionAction<MfgModel,FinishManufacturingPayload>(stmTransitionActionResolver,
						"finishManufacturing",1) {
			@Override
			public void transitionTo(MfgModel stateEntity, FinishManufacturingPayload transitionParam, State startState, String eventId, State endState, STMInternalTransitionInvoker<?> stm, Transition transition) throws Exception {
				stateEntity.secondTester = "Second Testing Done!";
			}
		};
	}

}

