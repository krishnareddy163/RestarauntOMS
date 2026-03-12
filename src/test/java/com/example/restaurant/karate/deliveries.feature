Feature: Delivery endpoints

  Background:
    * url baseUrl

  Scenario: Assign, update, and complete delivery flow
    Given path '/api/v1/orders'
    And request
      """
      {
        "customerId": 1,
        "deliveryAddress": "789 Oak St",
        "deliveryType": "DELIVERY",
        "items": [
          { "menuItemId": 1, "quantity": 1 }
        ]
      }
      """
    When method post
    Then status 201
    * def orderId = response.id

    Given path '/api/v1/deliveries', orderId, 'assign'
    When method post
    Then status 201
    And match response.status == 'ASSIGNED'
    And match response.driverId == 3
    * def deliveryId = response.id

    Given path '/api/v1/deliveries/driver', 3
    When method get
    Then status 200
    And match response[0].driverId == 3

    Given path '/api/v1/deliveries', deliveryId, 'pickup'
    When method patch
    Then status 200
    And match response.status == 'PICKED_UP'

    Given path '/api/v1/deliveries', deliveryId, 'location'
    And param latitude = '12.9716'
    And param longitude = '77.5946'
    When method patch
    Then status 200
    And match response.status == 'IN_TRANSIT'

    Given path '/api/v1/deliveries', deliveryId, 'complete'
    When method patch
    Then status 200
    And match response.status == 'COMPLETED'

    Given path '/api/v1/deliveries/driver', 3
    When method get
    Then status 200
    And match response == []
