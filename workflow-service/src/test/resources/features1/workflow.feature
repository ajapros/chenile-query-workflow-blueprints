Feature: Tests the Chenile Workflow Service using a REST client. A mfg service exists and is under test.
It helps to create a MfgModel and manages the state of the MfgModel as follows:
INITIATED -(putInAssemblyLine) -> IN_ASSEMBLY_LINE -> (finishManufacturing) -> OUT_OF_ASSEMBLY_LINE
 
  Scenario: Test create MfgModel
    When I POST a REST request to URL "/mfg" with payload
    """json
    {
	}
	"""
    Then the REST response contains key "mutatedEntity"
    And store "$.payload.mutatedEntity.id" from response to "id"
    And the REST response key "mutatedEntity.currentState.stateId" is "INITIATED"
	  
	Scenario: Retrieve the MfgModel that just got created
		When I GET a REST request to URL "/mfg/${id}"
		Then the REST response contains key "mutatedEntity"
	  And the REST response key "mutatedEntity.id" is "${id}"
	  And the REST response key "mutatedEntity.currentState.stateId" is "INITIATED"

  Scenario: Perform step putInAssemblyLine on the mfgModel with comments
    Given that "comment" equals "Dispatched to factory for manufacturing"
    And that "event" equals "putInAssemblyLine"
    When I PATCH a REST request to URL "/mfg/${id}/${event}" with payload
      """json
      {
          "comment": "${comment}"
      }
      """
    Then the REST response contains key "mutatedEntity"
    And the REST response key "mutatedEntity.id" is "${id}"
    And the REST response key "mutatedEntity.currentState.stateId" is "IN_ASSEMBLY_LINE"
    And the REST response key "mutatedEntity.comments.putInAssemblyLine" is "${comment}"

  Scenario: Perform step finishManufacturing on the mfgModel with comments. Also supply a modelType.
    Given that "comment" equals "Finished manufacturing a RETRO model"
    And that "modelType" equals "RETRO"
    And that "event" equals "finishManufacturing"
    When I PATCH a REST request to URL "/mfg/${id}/${event}" with payload
      """json
      {
          "comment": "${comment}",
          "modelType": "${modelType}"
      }
      """
    Then the REST response does not contain key "mutatedEntity"
    And success is false
    And the http status code is 400
    And the top level subErrorCode is 49000

  Scenario: Perform activity build on the mfgModel with comments.
    Given that "comment" equals "Building the model."
    And that "event" equals "build"
    When I PATCH a REST request to URL "/mfg/${id}/${event}" with payload
      """json
      {
          "comment": "${comment}"
      }
      """
    Then the REST response contains key "mutatedEntity"
    And the REST response key "mutatedEntity.id" is "${id}"
    And the REST response key "mutatedEntity.currentState.stateId" is "IN_ASSEMBLY_LINE"
    And the REST response key "mutatedEntity.activities" collection has an item with keys and values:
      | key     | value         |
      | name    | ${event}      |
      | comment | ${comment}    |

  Scenario: Perform activity fine-tune on the mfgModel with comments.
    Given that "comment" equals "Fine tuning the configurations."
    And that "event" equals "fine-tune"
    When I PATCH a REST request to URL "/mfg/${id}/${event}" with payload
      """json
      {
          "comment": "${comment}"
      }
      """
    Then the REST response contains key "mutatedEntity"
    And the REST response key "mutatedEntity.id" is "${id}"
    And the REST response key "mutatedEntity.currentState.stateId" is "IN_ASSEMBLY_LINE"
    And the REST response key "mutatedEntity.activities" collection has an item with keys and values:
      | key     | value         |
      | name    | ${event}      |
      | comment | ${comment}    |


  Scenario: Perform activity inAssemblyTesting on the mfgModel with comments.
    Given that "comment" equals "Testing the model in the assembly line."
    And that "event" equals "inAssemblyTesting"
    When I PATCH a REST request to URL "/mfg/${id}/${event}" with payload
      """json
      {
          "comment": "${comment}"
      }
      """
    Then the REST response contains key "mutatedEntity"
    And the REST response key "mutatedEntity.id" is "${id}"
    And the REST response key "mutatedEntity.currentState.stateId" is "IN_ASSEMBLY_LINE"
    And the REST response key "mutatedEntity.activities" collection has an item with keys and values:
      | key     | value         |
      | name    | ${event}      |
      | comment | ${comment}    |

  Scenario: Perform step finishManufacturing on the mfgModel with comments. Also supply a modelType.
    Given that "comment" equals "Finished manufacturing a RETRO model"
    And that "modelType" equals "RETRO"
    And that "event" equals "finishManufacturing"
    When I PATCH a REST request to URL "/mfg/${id}/${event}" with payload
      """json
      {
          "comment": "${comment}",
          "modelType": "${modelType}"
      }
      """
    Then the REST response contains key "mutatedEntity"
    And the REST response key "mutatedEntity.id" is "${id}"
    And the REST response key "mutatedEntity.currentState.stateId" is "OUT_OF_ASSEMBLY_LINE"
    And the REST response key "mutatedEntity.comments.finishManufacturing" is "${comment}"
    And the REST response key "mutatedEntity.modelType" is "${modelType}"
    And the response key "mutatedEntity.secondTester" is "Second Testing Done!"

  Scenario: Perform activity testByExperts on the mfgModel with comments.
    Given that "comment" equals "Expert Robert Brown tested the model successfully."
    And that "event" equals "testByExperts"
    When I PATCH a REST request to URL "/mfg/${id}/${event}" with payload
      """json
      {
          "comment": "${comment}"
      }
      """
    Then the REST response contains key "mutatedEntity"
    And the REST response key "mutatedEntity.id" is "${id}"
    And the REST response key "mutatedEntity.currentState.stateId" is "OUT_OF_ASSEMBLY_LINE"
    And the REST response key "mutatedEntity.activities" collection has an item with keys and values:
     | key     | value         |
     | name    | ${event}      |
     | comment | ${comment}    |

  Scenario: Perform optional activity checkIfPaintOk on the mfgModel with comments. This will not affect the state of the state entity.
    Given that "comment" equals "The painting job was great."
    And that "event" equals "checkIfPaintOk"
    When I PATCH a REST request to URL "/mfg/${id}/${event}" with payload
      """json
      {
          "comment": "${comment}"
      }
      """
    Then the REST response contains key "mutatedEntity"
    And the REST response key "mutatedEntity.id" is "${id}"
    And the REST response key "mutatedEntity.currentState.stateId" is "OUT_OF_ASSEMBLY_LINE"
    And the REST response key "mutatedEntity.activities" collection has an item with keys and values:
      | key     | value         |
      | name    | ${event}      |
      | comment | ${comment}    |

  Scenario: Add a new mandatory activity performSafetyInspection for the OUT_OF_ASSEMBLY_LINE state.
  Send safetyInspection event on the mfgModel with comments. This will not affect the state of the state entity.
    Given that config strategy is "mfgConfigProvider"
    And that a new mandatory activity "performSafetyInspection" is added from state "OUT_OF_ASSEMBLY_LINE" to state "AreOutOfAssemblyLineActivitiesComplete" in flow "MFG_FLOW"
    And that "comment" equals "Product passes all safety inspections."
    And that "event" equals "performSafetyInspection"
    When I PATCH a REST request to URL "/mfg/${id}/${event}" with payload
      """json
      {
          "comment": "${comment}"
      }
      """
    Then the REST response contains key "mutatedEntity"
    And the REST response key "mutatedEntity.id" is "${id}"
    And the REST response key "mutatedEntity.currentState.stateId" is "OUT_OF_ASSEMBLY_LINE"
    And the REST response key "mutatedEntity.activities" collection has an item with keys and values:
      | key     | value         |
      | name    | ${event}      |
      | comment | ${comment}    |

  Scenario: Perform activity testByEndUsers on the mfgModel with comments.
    Given that "comment" equals "End user Joe found that the system is working satisfactorily."
    And that "event" equals "testByEndUsers"
    When I PATCH a REST request to URL "/mfg/${id}/${event}" with payload
      """json
      {
          "comment": "${comment}"
      }
      """
    Then the REST response contains key "mutatedEntity"
    And the REST response key "mutatedEntity.id" is "${id}"
    And the REST response key "mutatedEntity.currentState.stateId" is "READY"
    And the REST response key "mutatedEntity.activities" collection has an item with keys and values:
      | key      | value       |
      | name     | ${event}    |
      | comment  | ${comment}  |


  Scenario: Perform sales activity invoice on the mfgModel with comments.
    Given that "comment" equals "Product Invoiced to ABC company which wants to buy it."
    And that "event" equals "invoice"
    When I PATCH a REST request to URL "/mfg/${id}/${event}" with payload
      """json
      {
          "comment": "${comment}"
      }
      """
    Then the REST response contains key "mutatedEntity"
    And the REST response key "mutatedEntity.id" is "${id}"
    And the REST response key "mutatedEntity.currentState.stateId" is "READY"
    And the REST response key "mutatedEntity.activities" collection has an item with keys and values:
      | key      | value       |
      | name     | ${event}    |
      | comment  | ${comment}  |

  Scenario: Perform sales activity pickUp on the mfgModel with comments.
  Before that add a "ship" activity to the READY stage. The auto state has to wait till both the
  activities are complete.
    Given that config strategy is "mfgConfigProvider"
    And that a new mandatory activity "ship" is added from state "READY" to state "AreSalesActivitiesComplete" in flow "MFG_FLOW"
    And that "comment" equals "Product picked up by the representative of ABC company."
    And that "event" equals "pickUp"
    When I PATCH a REST request to URL "/mfg/${id}/${event}" with payload
      """json
      {
          "comment": "${comment}"
      }
      """
    Then the REST response contains key "mutatedEntity"
    And the REST response key "mutatedEntity.id" is "${id}"
    And the REST response key "mutatedEntity.currentState.stateId" is "READY"
    And the REST response key "mutatedEntity.activities" collection has an item with keys and values:
      | key      | value       |
      | name     | ${event}    |
      | comment  | ${comment}  |

  Scenario: Perform sales activity ship on the mfgModel with comments.
    And that "comment" equals "Product picked up by the representative of ABC company."
    And that "event" equals "ship"
    When I PATCH a REST request to URL "/mfg/${id}/${event}" with payload
      """json
      {
          "comment": "${comment}"
      }
      """
    Then the REST response contains key "mutatedEntity"
    And the REST response key "mutatedEntity.id" is "${id}"
    And the REST response key "mutatedEntity.currentState.stateId" is "SOLD"
    And the REST response key "mutatedEntity.activities" collection has an item with keys and values:
      | key      | value       |
      | name     | ${event}    |
      | comment  | ${comment}  |