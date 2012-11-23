Feature: Offerwall, misc

  Scenario: I launch the offerwall with bad appid
    Given that I am user "tester" with valid credentials for "1246a"
    And that I am on production
    And that I launch the OfferWall
    And I wait for the OfferWall to become visible
    * I take a screenshot
    Then I see a "Bad request!" page

  Scenario: I open offerwall without credentials
    Given that I am on production
    And that I launch the OfferWall
    Then I get an error for missing credentials
