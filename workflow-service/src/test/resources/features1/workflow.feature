Feature: Tests the Chenile Workflow Service using a REST client. A mfg service exists and is under test.
It helps to create a MfgModel and manages the state of the MfgModel as follows:
INITIATED -(doS1) -> S1 -> (doS2) -> S2
 
  Scenario: Test create MfgModel
    When I POST a REST request to URL "/mfg" with payload
    """json
    {
	}
	"""
    Then the REST response contains key "mutatedEntity"
    And store "$.payload.mutatedEntity.id" from  response to "id"
    And the REST response key "mutatedEntity.currentState.stateId" is "INITIATED"
	  
	Scenario: Retrieve the MfgModel that just got created
		When I GET a REST request to URL "/mfg/${id}"
		Then the REST response contains key "mutatedEntity"
	  And the REST response key "mutatedEntity.id" is "${id}"
	  And the REST response key "mutatedEntity.currentState.stateId" is "INITIATED"

  Scenario: Perform step doS1 on the mfgModel with comments
    Given that "comment" equals "Performed S1"
    When I PATCH a REST request to URL "/mfg/${id}/doS1" with payload
      """json
      {
          "comment": "${comment}"
      }
      """
    Then the REST response contains key "mutatedEntity"
    And the REST response key "mutatedEntity.id" is "${id}"
    And the REST response key "mutatedEntity.currentState.stateId" is "S1"
    And the REST response key "mutatedEntity.comments.doS1" is "${comment}"

  Scenario: Perform step doS2 on the mfgModel with comments
    Given that "comment" equals "Performed S2"
    And that "s2Strategy" equals "s2 strategy"
    When I PATCH a REST request to URL "/mfg/${id}/doS2" with payload
      """json
      {
          "comment": "${comment}",
          "s2Strategy": "${s2Strategy}"
      }
      """
    Then the REST response contains key "mutatedEntity"
    And the REST response key "mutatedEntity.id" is "${id}"
    And the REST response key "mutatedEntity.currentState.stateId" is "S2"
    And the REST response key "mutatedEntity.comments.doS2" is "${comment}"
    And the REST response key "mutatedEntity.s2Strategy" is "${s2Strategy}"