Feature: Offerwall, valid user/appid on Production
  
  Background:
    Given that I am on production
    And that I am a valid user with credentials
    And that I launch the OfferWall
    And I wait for the OfferWall to become visible
 
  @1.3
  Scenario: Request OFW with valid *appid*, *userid*, *token*  
    Then I take a screenshot
    And I should see some offers

  @1.5 @manual_check
  Scenario: I am able to click the offer with LPID 89542
    Given that offer with LPID 89542 is available
    When I press offer with LPID 89542
    Then I take a screenshot
    And I manually check that the correct page is open

  @1.6
  Scenario: I am able to go to the support page and get back to offers
    Given I click on the support button
    And I take a screenshot
    When I click on the back button
    Then I should see some offers

  @1.6
  Scenario: I am able to go to the help page and get back to offers
    Given I click on the help button
    And I take a screenshot
    When I click on the back button
    Then I should see some offers

  @1.6
  Scenario: I am able to go to the privacy page and get back to offers
    Given I click on the privacy button
    And I take a screenshot
    When I click on the back button
    Then I should see some offers
  
  @not_on_test_plan
  Scenario: I am able to close the offerwall
    * I take a screenshot
    Given I click on the back button
    Then I see the main activity