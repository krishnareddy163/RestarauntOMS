Feature: Auth endpoints

  Scenario: Signup returns success message
    Given url baseUrl + '/api/v1/auth/signup'
    And request
      """
      {
        "email": "newuser@test.com",
        "password": "password123",
        "name": "New User",
        "phone": "3333333333",
        "role": "customer"
      }
      """
    When method post
    Then status 200
    And match response.message == 'User registered successfully!'

  Scenario: Signin fails with invalid credentials
    Given url baseUrl + '/api/v1/auth/signin'
    And request
      """
      {
        "email": "missing@test.com",
        "password": "wrongpass"
      }
      """
    When method post
    Then status 400
    And match response.message == 'Invalid email or password'

  Scenario: Signout returns success message
    Given url baseUrl + '/api/v1/auth/signout'
    When method post
    Then status 200
    And match response.message == "You've been signed out!"
