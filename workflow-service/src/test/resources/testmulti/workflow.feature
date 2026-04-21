Feature: Tests subtype-specific workflow payload resolution through the Chenile controller.
  The same event id dispatches different payload types and actions for Bus and Car entities.

  Scenario: Create a bus vehicle
    When I POST a REST request to URL "/vehicles" with payload
    """
    {
      "vehicleType": "BUS",
      "description": "Airport shuttle"
    }
    """
    Then the REST response contains key "mutatedEntity"
    And store "$.payload.mutatedEntity.id" from response to "busId"
    And the REST response key "mutatedEntity.currentState.stateId" is "OPEN"
    And the REST response key "mutatedEntity.vehicleType" is "BUS"

  Scenario: Dispatch the bus using the bus-specific payload
    When I PUT a REST request to URL "/vehicles/${busId}/dispatch" with payload
    """
    {
      "routeCode": "R-44",
      "seatCapacity": 48,
      "comment": "Bus dispatch"
    }
    """
    Then the REST response contains key "mutatedEntity"
    And the REST response key "mutatedEntity.id" is "${busId}"
    And the REST response key "mutatedEntity.currentState.stateId" is "DISPATCHED"
    And the REST response key "mutatedEntity.routeCode" is "R-44"
    And the REST response key "mutatedEntity.seatCapacity" is "48"
    And the REST response key "mutatedEntity.dispatchComment" is "Bus dispatch"

  Scenario: Create a car vehicle
    When I POST a REST request to URL "/vehicles" with payload
    """
    {
      "vehicleType": "CAR",
      "description": "City taxi"
    }
    """
    Then the REST response contains key "mutatedEntity"
    And store "$.payload.mutatedEntity.id" from response to "carId"
    And the REST response key "mutatedEntity.currentState.stateId" is "OPEN"
    And the REST response key "mutatedEntity.vehicleType" is "CAR"

  Scenario: Dispatch the car using the car-specific payload
    When I PUT a REST request to URL "/vehicles/${carId}/dispatch" with payload
    """
    {
      "garageCode": "G-12",
      "chargingSlot": "CS-9",
      "comment": "Car dispatch"
    }
    """
    Then the REST response contains key "mutatedEntity"
    And the REST response key "mutatedEntity.id" is "${carId}"
    And the REST response key "mutatedEntity.currentState.stateId" is "DISPATCHED"
    And the REST response key "mutatedEntity.garageCode" is "G-12"
    And the REST response key "mutatedEntity.chargingSlot" is "CS-9"
    And the REST response key "mutatedEntity.dispatchComment" is "Car dispatch"

  Scenario: Complete the bus with a generic event action
    When I PUT a REST request to URL "/vehicles/${busId}/complete" with payload
    """
    {
      "comment": "Bus complete"
    }
    """
    Then the REST response contains key "mutatedEntity"
    And the REST response key "mutatedEntity.id" is "${busId}"
    And the REST response key "mutatedEntity.currentState.stateId" is "COMPLETED"
    And the REST response key "mutatedEntity.completionComment" is "Bus complete"

  Scenario: Retrieve the car after dispatch
    When I GET a REST request to URL "/vehicles/${carId}"
    Then the REST response contains key "mutatedEntity"
    And the REST response key "mutatedEntity.id" is "${carId}"
    And the REST response key "mutatedEntity.currentState.stateId" is "DISPATCHED"
    And the REST response key "mutatedEntity.garageCode" is "G-12"
    And the REST response key "mutatedEntity.dispatchComment" is "Car dispatch"
