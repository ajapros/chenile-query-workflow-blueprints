Feature: Tests the Tenant Chenile Workflow with t Prefix.
	It helps to create an issue and manages the state of the issue as follows:
	OPENED -(assign) -> ASSIGNED -(resolve) -> RESOLVED -(close) -> CLOSED

	Scenario: Test create issue
		When I construct a REST request with header "x-chenile-tenant-id" and value "tenant0"
		And I POST a REST request to URL "/tissue" with payload
    """
    {
	    "openedBy": "USER1",
	    "description": "Unable to login to my mail account"
		}
		"""
		Then the REST response contains key "mutatedEntity"
		And store "$.payload.mutatedEntity.id" from response to "id"
		And the REST response key "mutatedEntity.currentState.stateId" is "OPENED"
		And the REST response key "mutatedEntity.openedBy" is "USER1"

	Scenario: Retrieve the issue that just got created
		When I construct a REST request with header "x-chenile-tenant-id" and value "tenant0"
		And I GET a REST request to URL "/tissue/${id}"
		Then the REST response contains key "mutatedEntity"
		And the REST response key "mutatedEntity.id" is "${id}"
		And the REST response key "mutatedEntity.currentState.stateId" is "OPENED"
		And the REST response key "mutatedEntity.openedBy" is "USER1"

	Scenario: Assign the issue to an assignee with comments
		When I construct a REST request with header "x-chenile-tenant-id" and value "tenant0"
		And I PUT a REST request to URL "/tissue/${id}/assign" with payload
		"""
		{
			"assignee": "MY-ASSIGNEE",
			"comment": "MY-ASSIGNEE-CAN-FIX-THIS"
		}
		"""
		Then the REST response contains key "mutatedEntity"
		And the REST response key "mutatedEntity.id" is "${id}"
		And the REST response key "mutatedEntity.currentState.stateId" is "ASSIGNED"
		And the REST response key "mutatedEntity.assignee" is "tenant0"
		And the REST response key "mutatedEntity.assignComment" is "MY-ASSIGNEE-CAN-FIX-THIS"



	  