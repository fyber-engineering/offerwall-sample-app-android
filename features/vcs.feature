Feature: VCS on production

  Background:
    Given that I am user "tester" with valid credentials for "1246"
    And that I am on production

  Scenario: I request coins without security token
    Given I request coins
    Then I get an error for missing security token
    * I take a screenshot

  Scenario: I request coins with an invalid security token
    Given I use "bad_token" as security token
    And I request coins
    Then I get an error for invalid signature
    * I take a screenshot

  Scenario: I request coins with invalid appid
    Given that I am user "tester" with valid credentials for "1246a"
    And I use "dont_care_token" as security token
    And I request coins
    Then I get an error for invalid application id
    * I take a screenshot

  Scenario: I request coins with a valid security token
    Given I use "12345678" as security token
    And I request coins
    Then I get some coins
    * I take a screenshot
    
  Scenario: I request coins twice and get no coins the second time
    Given I use "12345678" as security token
    And I request coins
    * I take a screenshot
    Then I dismiss the dialog
    And I request coins
    Then I get no coins
    * I take a screenshot