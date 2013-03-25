Feature: VCS on production

  Background:
    Given that I am a valid user with credentials
    And that I am on production

  @2.1 @2.2 @2.3 @2.4 @2.6 @2.8 @manual_check
  Scenario: I complete the test offer and I get coins
    Given that I am an unique user with credentials
    And I have correct security token
    When I request coins
    Then I get no coins
    And I have no transactions yet
    Then I dismiss the dialog
    Given that I launch the OfferWall
    And I wait for the OfferWall to become visible
    Given that offer with LPID 91642 is available
    Then I press offer with LPID 91642
    And I wait for 5 seconds
    Given I restart the application
    When I request coins
    Then I take a picture
    And I get some coins
    And I manually check that the toast message is visible

  @2.5
  Scenario: I request coins with invalid appid
    Given that I am a valid user with credentials for invalid app
    And I use "dont_care_token" as security token
    When I request coins
    Then I get an error for invalid application id

  @2.7
  Scenario: I request coins with an invalid security token
    Given I use "bad_token" as security token
    When I request coins
    Then I get an error for invalid signature

  @2.9
  Scenario: I request coins without security token
    When I request coins
    Then I get an error for missing security token