Feature: Spring Integration

  Scenario: Server health check passes
    When the client calls actuator health check
    Then the client receives status code of 200
    And the client receives a healthy response