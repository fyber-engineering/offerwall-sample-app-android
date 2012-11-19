Feature: Offerwall, valid user on Production
  
  Background:
    Given that I am user "tester" with valid credentials for "1246"
    And that I am on production
    And that I launch the OfferWall
    And I wait for the OfferWall to become visible
 
  Scenario: I am able to launch the OfferWall  
    Then I take a screenshot
    And I should see some offers

  Scenario: I am able to go to the support page and get back to offers
    Given I click on the support button
    And I take a screenshot
    Then I click on the back button

  Scenario: I am able to go to the help page and get back to offers
    Given I click on the help button
    And I take a screenshot
    Then I click on the back button

  Scenario: I am able to go to the privacy page and get back to offers
    Given I click on the privacy button
    And I take a screenshot
    Then I click on the back button
  
  Scenario: I am able to close the offerwall
    * I take a screenshot
    Given I click on the back button
    Then I see the main activity

  Scenario: I am able to click the offer with LPID 27067
    * I take a screenshot 
    Given that offer with LPID 27067 is available
    * I take a screenshot
    Then I press offer with LPID 27067

