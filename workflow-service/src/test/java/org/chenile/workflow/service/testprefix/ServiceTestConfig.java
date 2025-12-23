package org.chenile.workflow.service.testprefix;


import org.chenile.stm.STM;
import org.chenile.stm.STMFlowStore;
import org.chenile.stm.action.STMTransitionAction;
import org.chenile.stm.impl.BeanFactoryAdapter;
import org.chenile.stm.impl.STMActionsInfoProvider;
import org.chenile.stm.impl.STMFlowStoreImpl;
import org.chenile.stm.impl.STMImpl;
import org.chenile.stm.impl.XmlFlowReader;
import org.chenile.stm.spring.SpringBeanFactoryAdapter;
import org.chenile.workflow.param.MinimalPayload;
import org.chenile.workflow.service.activities.ActivityChecker;
import org.chenile.workflow.service.impl.StateEntityServiceImpl;
import org.chenile.workflow.service.stmcmds.*;
import org.chenile.workflow.service.test1.cmds.PerformStep;
import org.chenile.workflow.service.test1.mfg.MfgModel;
import org.chenile.workflow.service.testprefix.issues.AssignIssueAction;
import org.chenile.workflow.service.testprefix.issues.CloseIssueAction;
import org.chenile.workflow.service.testprefix.issues.Issue;
import org.chenile.workflow.service.testprefix.issues.IssueEntityStore;
import org.chenile.workflow.service.testprefix.issues.ResolveIssueAction;
import org.chenile.workflow.service.testprefix.issues.tenant0.Tenant0AssignIssueAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ActiveProfiles;

@Configuration
@PropertySource("classpath:org/chenile/workflow/service/testprefix/TestWorkflowService.properties")
@SpringBootApplication(scanBasePackages = { "org.chenile.configuration","org.chenile.workflow.service.testprefix.controller" })
@ActiveProfiles("unittest")
public class ServiceTestConfig extends SpringBootServletInitializer{

	private static final String FLOW_DEFINITION_FILE = "org/chenile/workflow/service/testprefix/issues.xml";
	@Bean BeanFactoryAdapter issueBeanFactoryAdapter() {
		return new SpringBeanFactoryAdapter();
	}
	
	@Bean STMFlowStoreImpl issueFlowStore(@Qualifier("issueBeanFactoryAdapter") BeanFactoryAdapter issueBeanFactoryAdapter) throws Exception{
		STMFlowStoreImpl stmFlowStore = new STMFlowStoreImpl();
		stmFlowStore.setBeanFactory(issueBeanFactoryAdapter);
		return stmFlowStore;
	}
	
	@Bean XmlFlowReader issueFlowReader(@Qualifier("issueFlowStore") STMFlowStoreImpl flowStore) throws Exception{
		XmlFlowReader flowReader = new XmlFlowReader(flowStore);
        flowReader.setFilename(FLOW_DEFINITION_FILE);
		return flowReader;
	}
	
	@Bean STM<Issue> issueEntityStm(@Qualifier("issueFlowStore") STMFlowStoreImpl stmFlowStore) throws Exception{
		STMImpl<Issue> stm = new STMImpl<>();		
		stm.setStmFlowStore(stmFlowStore);
		return stm;
	}
	
	@Bean STMActionsInfoProvider issueActionsInfoProvider(@Qualifier("issueFlowStore") STMFlowStoreImpl stmFlowStore) {
		return new STMActionsInfoProvider(stmFlowStore);
	}
	
	@Bean IssueEntityStore issueEntityStore() {
		return new IssueEntityStore();
	}
	
	@Bean StateEntityServiceImpl<Issue> _issueStateEntityService_(
			@Qualifier("issueEntityStm") STM<Issue> stm,
			@Qualifier("issueActionsInfoProvider") STMActionsInfoProvider issueInfoProvider,
			@Qualifier("issueEntityStore") IssueEntityStore entityStore){
		return new StateEntityServiceImpl<>(stm, issueInfoProvider, entityStore);
	}
	
	// Now we start constructing the STM Components 
	
	@Bean GenericEntryAction<Issue> issueEntryAction(@Qualifier("issueEntityStore") IssueEntityStore entityStore,
			@Qualifier("issueActionsInfoProvider") STMActionsInfoProvider issueInfoProvider){
		return new GenericEntryAction<Issue>(entityStore,issueInfoProvider);
	}
	
	@Bean GenericExitAction<Issue> issueExitAction(){
		return new GenericExitAction<Issue>();
	}
	


	@Bean STMTransitionAction<Issue> defaultSTMTransitionAction(

	) {
		return new BaseTransitionAction<>();
	}



	@Bean
	STMTransitionActionResolver stmTransitionActionResolver(
			@Qualifier("defaultSTMTransitionAction") STMTransitionAction<Issue> defaultSTMTransitionAction){
		return new STMTransitionActionResolver("issue",defaultSTMTransitionAction, "chenile-tenant-id");
	}

	@Bean StmBodyTypeSelector issueBodyTypeSelector(
			@Qualifier("issueActionsInfoProvider") STMActionsInfoProvider issueInfoProvider,
			@Qualifier("stmTransitionActionResolver") STMTransitionActionResolver stmTransitionActionResolver) {
		return new StmBodyTypeSelector(issueInfoProvider,stmTransitionActionResolver);
	}

	@Bean ActivityChecker issueActivitiesChecker(@Qualifier("issueFlowStore") STMFlowStore stmFlowStore){
		return new ActivityChecker(stmFlowStore);
	}

	@Bean STMTransitionAction<Issue> issueSTMTransitionAction(
			@Qualifier("stmTransitionActionResolver") STMTransitionActionResolver stmTransitionActionResolver,
			@Qualifier("issueActivitiesChecker") ActivityChecker issueActivitiesChecker){
		BaseTransitionAction<Issue> baseTransitionAction = new BaseTransitionAction<>(stmTransitionActionResolver);
		baseTransitionAction.activityChecker = issueActivitiesChecker;
		return baseTransitionAction;
	}


	@Bean AssignIssueAction issueAssign() {
		return new AssignIssueAction();
	}
	
	@Bean ResolveIssueAction issueResolve() {
		return new ResolveIssueAction();
	}
	
	@Bean CloseIssueAction issueClose() {
		return new CloseIssueAction();
	}


	// Tenant0 fields

	@Bean
	Tenant0AssignIssueAction tenant0IssueAssign(){return new Tenant0AssignIssueAction();}
	
}

