package org.chenile.cucumber.workflow;

import cucumber.api.java.en.Given;
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

    @Given("that config strategy is {string}")
    public void that_enablement_strategy_is(String beanName) {
        configProvider = (ConfigProviderImpl) applicationContext.getBean(beanName);
    }

    @Given("that a new mandatory activity {string} is added from state {string} to state {string} in flow {string}")
    public void that_a_new_mandatory_activity_is_added_to_state_in_flow(String activityName,
                String fromStateId, String toStateId, String flowId) throws Exception {
        configProvider.setProperties("""
				# Add activityName  to fromStateId  that will lead to toStateId
				%s.%s.transition.add.to.%s=%s
				## Make activity mandatory by setting the metadata activity to MANDATORY
				%s.%s.%s.meta.activity=MANDATORY
				""".formatted(flowId,fromStateId,toStateId,activityName,
                flowId,fromStateId,activityName));
    }

    @Given("that a new optional activity {string} is added from state {string} to state {string} in flow {string}")
    public void that_a_new_optional_activity_is_added_to_state_in_flow(String activityName,
                String fromStateId, String toStateId, String flowId) throws Exception {
        configProvider.setProperties("""
				# Add activityName  to fromStateId  that will lead to toStateId
				%s.%s.transition.add.to.%s=%s
				## Make activity optional by setting the metadata activity to OPTIONAL
				%s.%s.%s.meta.activity=OPTIONAL
				""".formatted(flowId,fromStateId,toStateId,activityName,
                flowId,fromStateId,activityName));
    }

    @Given("that all enablements are cleared")
    public void that_all_enablements_are_cleared()  {
        configProvider.clear();
    }

    @Given("that a new state {string} is added to flow {string}")
    public void that_a_new_state_is_added_to_flow(String stateId, String flowId) throws Exception {
        configProvider.setProperties("""
				# Add state to flow
				state.add.%s.in=%s
				""".formatted(stateId,flowId));
    }

    @Given("that a new transition {string} is added from state {string} to state {string} in flow {string}")
    public void that_a_new_transition_is_added_to_state_in_flow(
            String eventId, String fromState, String toState, String flowId) throws Exception {
        configProvider.setProperties("""
				# Add a new transition eventId from fromState to toState in flowId
				%s.%s.transition.add.to.%s=%s
				""".formatted(flowId,fromState,toState,eventId));
    }

    @Given("that state {string} in flow {string} is enabled")
    public void that_state_is_enabled(
            String stateId, String flowId) throws Exception {
        configProvider.setProperties("""
				# Enable state
				%s.%s.enabled=true
				""".formatted(flowId,stateId));
    }

    @Given("that state {string} in flow {string} is disabled")
    public void that_state_is_disabled(
            String stateId, String flowId) throws Exception {
        configProvider.setProperties("""
				# Disable state
				%s.%s.enabled=false
				""".formatted(flowId,stateId));
    }

    @Given("that transition {string} in state {string} in flow {string} is enabled")
    public void that_transition_in_state_in_flow_is_enabled(
            String eventId, String stateId, String flowId) throws Exception {
        configProvider.setProperties("""
				# enable state transition
				%s.%s.%s.enabled=true
				""".formatted(flowId,stateId,eventId));
    }

    @Given("that transition {string} for state {string} in flow {string} is disabled")
    public void that_transition_for_state_in_flow_is_disabled(
            String eventId, String stateId, String flowId) throws Exception {
        configProvider.setProperties("""
				# disable state transition
				%s.%s.%s.enabled=false
				""".formatted(flowId,stateId,eventId));
    }

    @Given("that state {string} in flow {string} has metadata with key {string} and value {string}")
    public void that_state_in_flow_has_metadata_with_key_value(
            String stateId, String flowId, String key, String value) throws Exception {
        configProvider.setProperties("""
				# introduce metadata with key and value for state
				%s.%s.meta.%s=%s
				""".formatted(flowId,stateId,key,value));
    }

    @Given("that transition {string} from state {string} in flow {string} has metadata with key {string} and value {string}")
    public void that_transition_from_state_in_flow_has_metadata_with_key_value(
            String eventId, String stateId, String flowId, String key, String value) throws Exception {
        configProvider.setProperties("""
				# introduce metadata with key and value for state
				%s.%s.%s.meta.%s=%s
				""".formatted(flowId,stateId,eventId,key,value));
    }
}

