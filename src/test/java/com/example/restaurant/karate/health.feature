Feature: Health endpoints

  Scenario: Health check returns status UP
    Given url baseUrl + '/api/v1/health'
    When method get
    Then status 200
    And match response.status == 'UP'

  Scenario: Readiness probe returns ready true
    Given url baseUrl + '/api/v1/health/readiness'
    When method get
    Then status 200
    And match response.ready == true

  Scenario: Liveness probe returns alive true
    Given url baseUrl + '/api/v1/health/liveness'
    When method get
    Then status 200
    And match response.alive == true
