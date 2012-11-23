Feature: Unlock items, production

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
 
  Scenario: I open the unlock offerwall and see some offers
    Given I open unlock offerwall for item "DEFAULT_ITEM"
    And I wait for the OfferWall to become visible
    Then I should see some offers

  Scenario Outline: Check item descriptions
    Given I enter <itemName> as item name
    And I open unlock offerwall for item <itemId>
    * I wait for the OfferWall to become visible
    Then I see the item description <description>

    Examples:
    | itemName | itemId          | description   |
    | ""       | "DEFAULT_ITEM"  | "Test Item with a very, very long name really long name actually neverending name really its a neverending story this name" |
    | "abc"    | "DEFAULT_ITEM"  | "abc"         |
    | ""       | "ITEM_ID_1"     | "item name 1" |
    | ""       | "ITEM_ID_2"     | "item name 1" |

  Scenario: I open the unlock offerwall with bad appid
    Given that I am user "tester" with valid credentials for "1246a"
    And I open unlock offerwall for item "DEFAULT_ITEM"
    * I wait for the OfferWall to become visible
    Then I see a "Bad request!" page

  Scenario: I leave my user id empty and I request unlock offerwall with bad item id
    Given that I am user "" with valid credentials for "1246"
    And I open unlock offerwall for item "bad_item_id"
    Then I get an error for invalid item id