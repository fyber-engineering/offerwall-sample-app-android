Feature: Interstitial

  Background:
    Given that I am on the interstitial page
    And that I am on production
    #And that I am on staging

  Scenario: I request several interstitial one after the other
    Given that I am user "tester" with valid credentials for "1246"
    And that I launch the Interstitial
    Then I wait for the Interstitial to become visible
    * I take a screenshot
    Then I close the Interstitial
    Given that I launch the Interstitial
    Then I wait for the Interstitial to become visible
    * I take a screenshot
    When I close the Interstitial
    Then I see the main activity

  Scenario: I request twice an interstitial changing user id
    Given that I am user "testing_is_fun" with valid credentials for "1246"
    And that I launch the Interstitial
    Then I wait for the Interstitial to become visible
    * I take a screenshot
    Then I close the Interstitial
    Given that I am user "i_love_testing" with valid credentials for "1246"
    And that I launch the Interstitial
    Then I wait for the Interstitial to become visible
    * I take a screenshot
    Then I close the Interstitial
    And I manually check both screenshots

  Scenario: I request interstitial without any appid or userid
    When that I launch the Interstitial
    Then I get an error for missing credentials

  Scenario: I click on the download button
    Given that I am user "tester" with valid credentials for "1246"
    And that I launch the Interstitial
    Then I wait for the Interstitial to become visible
    * I take a screenshot
    When I click on the download button
    * I take a screenshot
    And I manually check it

  Scenario: I click on more apps and check the support and privacy buttons
    Given that I am user "tester" with valid credentials for "1246"
    And that I launch the Interstitial
    Then I wait for the Interstitial to become visible
    * I take a screenshot
    Then I click on more apps
    * I wait for 4 seconds
    When I click on the support button
    * I wait for 4 seconds
    * I take a screenshot
    Then I click on the back button
    * I wait for 4 seconds
    When I click on the privacy button
    * I wait for 4 seconds
    * I take a screenshot
    Then I click on the back button
    