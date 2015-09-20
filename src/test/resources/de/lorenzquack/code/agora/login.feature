Feature: Login
  As a user I want to be able to login to perform administration of agora

  Scenario: Redirect to login screen
    Given I am not logged in
    When I go to the administration address
    Then I am presented with the login screen
