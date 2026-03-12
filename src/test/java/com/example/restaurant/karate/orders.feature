Feature: Order endpoints

  Background:
    * url baseUrl

  Scenario: Create order and fetch by id
    Given path '/api/v1/orders'
    And request
      """
      {
        "customerId": 1,
        "deliveryAddress": "123 Main St",
        "deliveryType": "DELIVERY",
        "items": [
          { "menuItemId": 1, "quantity": 2 }
        ]
      }
      """
    When method post
    Then status 201
    And match response.status == 'CONFIRMED'
    * def orderId = response.id

    Given path '/api/v1/orders', orderId
    When method get
    Then status 200
    And match response.id == orderId
    And match response.customerId == 1

  Scenario: Get customer orders returns page content
    Given path '/api/v1/orders'
    And request
      """
      {
        "customerId": 1,
        "deliveryAddress": "999 Side St",
        "deliveryType": "DELIVERY",
        "items": [
          { "menuItemId": 1, "quantity": 1 }
        ]
      }
      """
    When method post
    Then status 201

    Given path '/api/v1/orders/customer', 1
    When method get
    Then status 200
    And match response.content[0].customerId == 1

  Scenario: Update order status to PREPARING
    Given path '/api/v1/orders'
    And request
      """
      {
        "customerId": 1,
        "deliveryAddress": "123 Main St",
        "deliveryType": "DELIVERY",
        "items": [
          { "menuItemId": 1, "quantity": 1 }
        ]
      }
      """
    When method post
    Then status 201
    * def orderId = response.id

    Given path '/api/v1/orders', orderId, 'status'
    And param status = 'PREPARING'
    When method patch
    Then status 200
