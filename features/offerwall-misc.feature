Feature: Offerwall, misc
  
  Background:
    Given I am on production

  @1.1
  Scenario: Request OFW with non existing *appid*
    Given that I have valid credentials for "131313"
    When I launch the OfferWall
    And I wait for the OfferWall to become visible
    * I take a screenshot
    Then I see a "Cookies required" page

  @1.2
  Scenario: Request OFW with not eligible *appid*
    Given that I am a valid user with credentials for invalid app
    When I launch the OfferWall
    And I wait for the OfferWall to become visible
    * I take a screenshot
    Then I see a "Bad request!" page

  @1.4
  Scenario: Request a new session with empty fields for *appid*, *userid*, *token*
    When I start the SDK
    Then I get an error for missing application id

  @1.12
  Scenario:

  @1.13
  Scenario: Request OFW with custom currency
    Given I am a valid user with credentials
    And that custom currency is set to "ololo"
    When I launch the OfferWall
    And I wait for the OfferWall to become visible
    Then the OFW currency is "ololo"

  @not_on_test_plan
  Scenario: I open offerwall without credentials
    When I launch the OfferWall
    Then I get an error for missing credentials