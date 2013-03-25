Feature: Interstitial

  Background:
    Given that I am on the interstitial page
    And that I am on production

  @5.1
  Scenario: Request Interstitial with invalid appid
    Given that I am a valid user with credentials for invalid app
    When I launch the Interstitial
    And I wait for 2 seconds
    Then the log ends with "Interstitial request completed with status code: 400, did trigger exception: false"

  @5.2 @5.5 @manual_check
  Scenario: Request Insterstitial with valid appid of live application
    Given I am a valid user with credentials
    When I launch the Interstitial
    And I wait for the Interstitial to become visible
    Then I take a screenshot
    When I click on the download button
    Then I take a screenshot
    And I manually check that I am redirected to the correct landing page

  @5.3
  Scenario: Request interstitial with valid appid of a not live application
    Given that I have valid credentials for "131313"
    When I launch the Interstitial
    Then the log ends with "Interstitial request completed with status code: 401, did trigger exception: false"

  @5.4 @manual_check
  Scenario:  Request Interstitial for different users but the same deviceid
    Given that I am user "testing_is_fun" with valid credentials
    When I launch the Interstitial
    And I wait for the Interstitial to become visible
    Then I take a screenshot
    And I close the Interstitial
    Given that I am user "i_love_testing" with valid credentials
    When I launch the Interstitial
    And I wait for the Interstitial to become visible
    Then I take a screenshot
    And I close the Interstitial
    And I manually check both screenshots

  @5.6
  Scenario: I request interstitial without any appid or userid
    When I launch the Interstitial
    Then I get an error for missing credentials

  @5.7
  Scenario: I click on more apps and check the support and privacy buttons
    Given I am a valid user with credentials
    When I launch the Interstitial
    And I wait for the Interstitial to become visible
    Then I click on more apps
    * I wait for 4 seconds
    When I click on the support button
    * I wait for 6 seconds
    Then I click on the back button
    * I wait for 6 seconds
    When I click on the privacy button
    * I wait for 6 seconds
    Then I click on the back button

  @5.9
  Scenario: Request Interstitial with custom currency
    Given I am a valid user with credentials
    And that custom currency is set to "ololo"
    When I launch the Interstitial
    And I wait for the Interstitial to become visible
    Then the currency is "ololo"

  @not_on_test_plan
  Scenario: I request several interstitial one after the other
    Given I am a valid user with credentials
    When I launch the Interstitial
    Then I wait for the Interstitial to become visible
    And I close the Interstitial
    When I launch the Interstitial
    And I wait for the Interstitial to become visible
    Then I close the Interstitial
    And I see the main activity