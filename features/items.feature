Feature: Unlock items, production

  Background:
    Given that I am a valid user with credentials
    And that I am on production
    And that I am on the items page

  @6.1
  Scenario: I request item status for a new user
    Given that I am an unique user with credentials
    When I request item status
    Then I wait for the Unlock Item status to become visible
    And I take a screenshot

  @6.3
  Scenario: Request unlock offer with appid for not existing application
    Given that I have valid credentials for "131313"
    When I open unlock offerwall for item "NU"
    And I wait for the Unlock OfferWall to become visible
    Then I see a "Cookies required" page

  @6.4
  Scenario: I launch unlock offerwall with an invalid Item ID
    When I open unlock offerwall for item "nu"
    Then I get an error for invalid item id

  @6.5
  Scenario: I open the unlock offerwall and see some offers
    Given I open unlock offerwall for item "NU"
    And I wait for the Unlock OfferWall to become visible
    Then I should see some offers

  @6.5
  Scenario Outline: Check item descriptions
    Given I enter <item_name> as item name
    And I open unlock offerwall for item <item_id>
    * I wait for the Unlock OfferWall to become visible
    Then I see the item description <description>

    Examples:
    | item_name | item_id   | description       |
    | ""        | "NU"      | "Name of Nu"      |
    | "abc"     | "NU"      | "abc"             |
    | ""        | "OLOLO"   | "Name of Ololo"   |
    | ""        | "TRALALA" | "Name of Tralala" |

  @6.6
  Scenario: Request Unlock OFW with non existing itemid
    Given I open unlock offerwall for item "NUMO"
    And I wait for the Unlock OfferWall to become visible
    Then I should not get any offer

  @6.7
  Scenario: I request item status with invalid security token
    Given I use "bad_token" as security token
    And I request item status
    * I take a screenshot
    Then I get an error for invalid signature

  @not_on_test_plan
  Scenario: I launch unlock offerwall without Item ID
    When I open unlock offerwall
    Then I get an error for invalid item id

  @not_on_test_plan
  Scenario: I request item status with invalid appid
    Given that I am a valid user with credentials for invalid app
    And I use "dont_care_token" as security token
    And I request item status
    Then I get an error for invalid application id
    * I take a screenshot

  @not_on_test_plan
  Scenario: I open the unlock offerwall with bad appid
    Given that I am a valid user with credentials for invalid app
    When I open unlock offerwall for item "NU"
    And I wait for the Unlock OfferWall to become visible
    Then I see a "Bad request!" page 

  @not_on_test_plan
  Scenario: I request item status without security token
    Given I request item status
    * I take a screenshot
    Then I get an error for missing security token

  @not_on_test_plan
  Scenario: I leave my user id empty and I request unlock offerwall with bad item id
    Given that I am user "" with valid credentials for "1246"
    And I open unlock offerwall for item "bad_item_id"
    Then I get an error for invalid item id