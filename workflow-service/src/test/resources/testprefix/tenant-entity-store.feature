Feature: Tenant-specific EntityStore discovery
  A workflow uses a tenant-specific EntityStore when one is registered and otherwise uses its base store.

  Scenario: Store and retrieve an issue through the tenant-specific EntityStore
    When I construct a REST request with header "x-chenile-tenant-id" and value "tenant0"
    And I POST a REST request to URL "/tissue" with payload
    """
    {
      "openedBy": "TENANT0-USER",
      "description": "Stored by tenant0"
    }
    """
    Then the REST response contains key "mutatedEntity"
    And the REST response key "mutatedEntity.id" contains string "tenant0-"
    And store "$.payload.mutatedEntity.id" from response to "tenant0EntityStoreId"
    When I construct a REST request with header "x-chenile-tenant-id" and value "tenant0"
    And I GET a REST request to URL "/tissue/${tenant0EntityStoreId}"
    Then the REST response key "mutatedEntity.openedBy" is "TENANT0-USER"

  Scenario: A tenant without a store falls back to the base EntityStore
    When I construct a REST request with header "x-chenile-tenant-id" and value "tenant1"
    And I POST a REST request to URL "/tissue" with payload
    """
    {
      "openedBy": "TENANT1-USER",
      "description": "Stored by the fallback store"
    }
    """
    Then the REST response contains key "mutatedEntity"
    And store "$.payload.mutatedEntity.id" from response to "tenant1FallbackEntityStoreId"
    When I construct a REST request with header "x-chenile-tenant-id" and value "tenant1"
    And I GET a REST request to URL "/tissue/${tenant1FallbackEntityStoreId}"
    Then the REST response key "mutatedEntity.openedBy" is "TENANT1-USER"

  Scenario: Tenant-specific data is not available through the fallback store
    When I construct a REST request with header "x-chenile-tenant-id" and value "tenant1"
    And I GET a REST request to URL "/tissue/${tenant0EntityStoreId}"
    Then the http status code is 404

  Scenario: No tenant context uses the base EntityStore
    When I POST a REST request to URL "/tissue" with payload
    """
    {
      "openedBy": "GENERIC-USER",
      "description": "Stored without a tenant"
    }
    """
    Then the REST response contains key "mutatedEntity"
    And store "$.payload.mutatedEntity.id" from response to "genericEntityStoreId"
    When I GET a REST request to URL "/tissue/${genericEntityStoreId}"
    Then the REST response key "mutatedEntity.openedBy" is "GENERIC-USER"

  Scenario: Tenant store takes precedence over configured context-key selection
    When I construct a REST request with header "x-chenile-tenant-id" and value "tenant0"
    And I construct a REST request with header "x-chenile-region-id" and value "apac"
    And I POST a REST request to URL "/tissue" with payload
    """
    {
      "openedBy": "TENANT-FIRST-USER",
      "description": "Tenant override wins"
    }
    """
    Then the REST response key "mutatedEntity.id" contains string "tenant0-"

  Scenario: Configured context-key store takes precedence over custom selection
    When I construct a REST request with header "x-chenile-tenant-id" and value "tenant1"
    And I construct a REST request with header "x-chenile-region-id" and value "apac"
    And I construct a REST request with header "x-chenile-apt" and value "custom"
    And I POST a REST request to URL "/tissue" with payload
    """
    {
      "openedBy": "APAC-USER",
      "description": "Selected by region"
    }
    """
    Then the REST response key "mutatedEntity.id" contains string "apac-"
    And store "$.payload.mutatedEntity.id" from response to "apacEntityStoreId"
    When I construct a REST request with header "x-chenile-tenant-id" and value "tenant1"
    And I construct a REST request with header "x-chenile-region-id" and value "apac"
    And I GET a REST request to URL "/tissue/${apacEntityStoreId}"
    Then the REST response key "mutatedEntity.openedBy" is "APAC-USER"

  Scenario: Application strategy selects a store after tenant and context-key misses
    When I construct a REST request with header "x-chenile-tenant-id" and value "tenant1"
    And I construct a REST request with header "x-chenile-apt" and value "custom"
    And I POST a REST request to URL "/tissue" with payload
    """
    {
      "openedBy": "CUSTOM-USER",
      "description": "Selected by application strategy"
    }
    """
    Then the REST response key "mutatedEntity.id" contains string "custom-"
    And store "$.payload.mutatedEntity.id" from response to "customEntityStoreId"
    When I construct a REST request with header "x-chenile-tenant-id" and value "tenant1"
    And I construct a REST request with header "x-chenile-apt" and value "custom"
    And I GET a REST request to URL "/tissue/${customEntityStoreId}"
    Then the REST response key "mutatedEntity.openedBy" is "CUSTOM-USER"
