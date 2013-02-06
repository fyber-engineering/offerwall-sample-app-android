Feature: Offerwall, misc

  Scenario: Request OFW with not eligible *appid*
    Given that I am user "tester" with valid credentials for "8025x"
    And that I am on production
    When I launch the OfferWall
    And I wait for the OfferWall to become visible
    * I take a screenshot
    Then I see a "Bad request!" page

  Scenario: I open offerwall without credentials
    Given that I am on production
    And that I launch the OfferWall
    Then I get an error for missing credentials

  Scenario: Request OfferWall with empty IDs
    Given that I start the SDK
    Then I get an error for missing application id
    Given I dismiss the dialog
    And I launch the OfferWall
    And I wait for 3 seconds
    Then I get an error for missing credentials