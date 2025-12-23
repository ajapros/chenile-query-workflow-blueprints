package org.chenile.cucumber.workflow;

import io.cucumber.java.en.Given;
import org.chenile.cucumber.VariableHelper;
import org.chenile.stm.impl.ConfigProviderImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

/**
 * Cucumber steps to facilitate running tests using Spring MOCK MVC.<br/>
 * These methods automate using the enablement strategy in the workflow
 * They also support the workflow activity framework.
 */
@ActiveProfiles("unittest")
public class CucumberWorkflowSteps {
    @Autowired
    ApplicationContext applicationContext;
    ConfigProviderImpl configProvider = null;
    String prefix = "";

    @Given("that config strategy is {string}")
    public void that_enablement_strategy_is(String beanName) {
        configProvider = (ConfigProviderImpl) applicationContext.getBean(beanName);
    }

    @Given("that config strategy is {string} with prefix {string}")
    public void that_enablement_strategy_is_with_prefix(String beanName,String prefix) {
        configProvider = (ConfigProviderImpl) applicationContext.getBean(beanName);
        this.prefix = prefix + ".";
    }

    @Given("that a new mandatory activity {string} is added from state {string} to state {string} in flow {string}")
    public void that_a_new_mandatory_activity_is_added_to_state_in_flow(String activityName,
                String fromStateId, String toStateId, String flowId) throws Exception {

        activityName = VariableHelper.substituteVariables(activityName);
        fromStateId = VariableHelper.substituteVariables(fromStateId);
        toStateId = VariableHelper.substituteVariables(toStateId);
        flowId = VariableHelper.substituteVariables(flowId);
        System.err.println("+++++++++++++Adding a mandatory activity " + activityName + " fro  state " + fromStateId
                + " to state " + toStateId + " in flow " + flowId);
        configProvider.setProperties("""
				# Add activityName  to fromStateId  that will lead to toStateId
				%s%s.%s.transition.add.%s=%s
				## Make activity mandatory by setting the metadata activity to MANDATORY
				%s%s.%s.%s.meta.activity=MANDATORY
				""".formatted(prefix,flowId,fromStateId,activityName,toStateId,
                prefix,flowId,fromStateId,activityName));
    }

    @Given("that a new optional activity {string} is added from state {string} to state {string} in flow {string}")
    public void that_a_new_optional_activity_is_added_to_state_in_flow(String activityName,
                String fromStateId, String toStateId, String flowId) throws Exception {
        activityName = VariableHelper.substituteVariables(activityName);
        fromStateId = VariableHelper.substituteVariables(fromStateId);
        toStateId = VariableHelper.substituteVariables(toStateId);
        flowId = VariableHelper.substituteVariables(flowId);
        configProvider.setProperties("""
				# Add activityName  to fromStateId  that will lead to toStateId
				%s%s.%s.transition.add.%s=%s
				## Make activity optional by setting the metadata activity to OPTIONAL
				%s%s.%s.%s.meta.activity=OPTIONAL
				""".formatted(prefix,flowId,fromStateId,activityName,toStateId,
                prefix,flowId,fromStateId,activityName));
    }

    @Given("that a new activity completion checker {string} is added from state {string} to state {string} in flow {string}")
    public void that_a_new_activity_completion_checker_is_added_to_state_in_flow(String activityName,
                String fromStateId, String toStateId, String flowId) throws Exception {
        activityName = VariableHelper.substituteVariables(activityName);
        fromStateId = VariableHelper.substituteVariables(fromStateId);
        toStateId = VariableHelper.substituteVariables(toStateId);
        flowId = VariableHelper.substituteVariables(flowId);
        System.err.println("+++++++++++++++++Adding a completion checker " + activityName + " fro  state " + fromStateId
                + " to state " + toStateId + " in flow " + flowId);
        configProvider.setProperties("""
				# Add activityName  to fromStateId  that will lead to toStateId
				%s%s.%s.transition.add.%s=%s
				## Make activity mandatory by setting the metadata activity to MANDATORY
				%s%s.%s.%s.meta.activity=COMPLETION_CHECKER
				""".formatted(prefix,flowId,fromStateId,activityName,toStateId,
                prefix,flowId,fromStateId,activityName));
    }

    @Given("that all enablements are cleared")
    public void that_all_enablements_are_cleared()  {
        configProvider.clear();
    }

    @Given("that a new state {string} is added to flow {string}")
    public void that_a_new_state_is_added_to_flow(String stateId, String flowId) throws Exception {

        stateId = VariableHelper.substituteVariables(stateId);
        flowId = VariableHelper.substituteVariables(flowId);
        configProvider.setProperties("""
				# Add state to flow
				%sstate.add.%s.in=%s
				""".formatted(prefix,stateId,flowId));
    }

    @Given("that a new transition {string} is added from state {string} to state {string} in flow {string}")
    public void that_a_new_transition_is_added_to_state_in_flow(
            String eventId, String fromState, String toState, String flowId) throws Exception {
        eventId = VariableHelper.substituteVariables(eventId);
        fromState = VariableHelper.substituteVariables(fromState);
        toState = VariableHelper.substituteVariables(toState);
        flowId = VariableHelper.substituteVariables(flowId);
        configProvider.setProperties("""
				# Add a new transition eventId from fromState to toState in flowId
				%s%s.%s.transition.add.%s=%s
				""".formatted(prefix,flowId,fromState,eventId,toState));
    }

    @Given("that state {string} in flow {string} is enabled")
    public void that_state_is_enabled(
            String stateId, String flowId) throws Exception {

        stateId = VariableHelper.substituteVariables(stateId);
        flowId = VariableHelper.substituteVariables(flowId);
        configProvider.setProperties("""
				# Enable state
				%s%s.%s.enabled=true
				""".formatted(prefix,flowId,stateId));
    }

    @Given("that state {string} in flow {string} is disabled")
    public void that_state_is_disabled(
            String stateId, String flowId) throws Exception {
        stateId = VariableHelper.substituteVariables(stateId);
        flowId = VariableHelper.substituteVariables(flowId);
        configProvider.setProperties("""
				# Disable state
				%s%s.%s.enabled=false
				""".formatted(prefix,flowId,stateId));
    }

    @Given("that transition {string} in state {string} in flow {string} is enabled")
    public void that_transition_in_state_in_flow_is_enabled(
            String eventId, String stateId, String flowId) throws Exception {
        eventId = VariableHelper.substituteVariables(eventId);
        stateId = VariableHelper.substituteVariables(stateId);
        flowId = VariableHelper.substituteVariables(flowId);
        configProvider.setProperties("""
				# enable state transition
				%s%s.%s.%s.enabled=true
				""".formatted(prefix,flowId,stateId,eventId));
    }

    @Given("that transition {string} for state {string} in flow {string} is disabled")
    public void that_transition_for_state_in_flow_is_disabled(
            String eventId, String stateId, String flowId) throws Exception {
        eventId = VariableHelper.substituteVariables(eventId);
        stateId = VariableHelper.substituteVariables(stateId);
        flowId = VariableHelper.substituteVariables(flowId);
        configProvider.setProperties("""
				# disable state transition
				%s%s.%s.%s.enabled=false
				""".formatted(prefix,flowId,stateId,eventId));
    }

    @Given("that state {string} in flow {string} has metadata with key {string} and value {string}")
    public void that_state_in_flow_has_metadata_with_key_value(
            String stateId, String flowId, String key, String value) throws Exception {

        stateId = VariableHelper.substituteVariables(stateId);
        flowId = VariableHelper.substituteVariables(flowId);
        key = VariableHelper.substituteVariables(key);
        value = VariableHelper.substituteVariables(value);
        configProvider.setProperties("""
				# introduce metadata with key and value for state
				%s%s.%s.meta.%s=%s
				""".formatted(prefix,flowId,stateId,key,value));
    }

    @Given("that transition {string} from state {string} in flow {string} has metadata with key {string} and value {string}")
    public void that_transition_from_state_in_flow_has_metadata_with_key_value(
            String eventId, String stateId, String flowId, String key, String value) throws Exception {
        eventId = VariableHelper.substituteVariables(eventId);
        stateId = VariableHelper.substituteVariables(stateId);
        flowId = VariableHelper.substituteVariables(flowId);
        key = VariableHelper.substituteVariables(key);
        value = VariableHelper.substituteVariables(value);
        configProvider.setProperties("""
				# introduce metadata with key and value for state
				%s%s.%s.%s.meta.%s=%s
				""".formatted(prefix,flowId,stateId,eventId,key,value));
    }
}

