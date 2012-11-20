Feature: Unlock items, valid user/appid on production

  Background:
    Given that I am user "tester" with valid credentials for "1246"
    And that I am on production
    And that I am on the items page

  Scenario: I launch unlock offerwall without Item ID
    Given I open unlock offerwall
    * I take a screenshot
    Then I get an error for invalid item id

  Scenario: I launch unlock offerwall with an invalid Item ID
    Given I open unlock offerwall for item "invalid id"
    * I take a screenshot
    Then I get an error for invalid item id

  Scenario: I request item status without security token
    Given I request item status
    * I take a screenshot
    Then I get an error for missing security token

  Scenario: I request item status with invalid security token
    Given I use "bad_token" as security token
    And I request item status
    * I take a screenshot
    Then I get an error for invalid signature

  Scenario: I request item status with invalid appid
    Given that I am user "tester" with valid credentials for "1246a"
    And I use "dont_care_token" as security token
    And I request item status
    Then I get an error for invalid application id
    * I take a screenshot