Feature: Payment endpoints

  Background:
    * url baseUrl

  Scenario: Process payment and fetch by id and order
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

    Given path '/api/v1/payments'
    And request
      """
      {
        "orderId": #(orderId),
        "amount": 9.99,
        "paymentMethod": "CREDIT_CARD"
      }
      """
    When method post
    Then status 201
    And match response.status == 'SUCCESS'
    * def paymentId = response.id

    Given path '/api/v1/payments', paymentId
    When method get
    Then status 200
    And match response.id == paymentId

    Given path '/api/v1/payments/order', orderId
    When method get
    Then status 200
    And match response.orderId == orderId

  Scenario: Refund successful payment
    Given path '/api/v1/orders'
    And request
      """
      {
        "customerId": 1,
        "deliveryAddress": "456 Elm St",
        "deliveryType": "DELIVERY",
        "items": [
          { "menuItemId": 1, "quantity": 1 }
        ]
      }
      """
    When method post
    Then status 201
    * def orderId = response.id

    Given path '/api/v1/payments'
    And request
      """
      {
        "orderId": #(orderId),
        "amount": 9.99,
        "paymentMethod": "DEBIT_CARD"
      }
      """
    When method post
    Then status 201
    * def paymentId = response.id

    Given path '/api/v1/payments', paymentId, 'refund'
    When method post
    Then status 200
    And match response.status == 'REFUNDED'
