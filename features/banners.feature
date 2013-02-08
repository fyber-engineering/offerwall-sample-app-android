Feature: Banners, production

  Background:
    Given that I am on production
    And that I am on the banners page

  @4.1 @manual_check
  Scenario: I touch the banner
    Given that I am a valid user with credentials
    When I request a banner
    And I wait for 5 seconds
    Then I see a banner
    And I take a screenshot
    When I touch the banner
    And I wait for 5 seconds
    Then I take a screenshot
    And I manually check the the correct page is opened

  @4.3
  Scenario: I request a banner with invalid appid
    Given that I am a valid user with credentials for invalid app
    When I request a banner
    And I wait for 5 seconds
    Then I see an error

  @4.4
  Scenario: I request a banner without credentials
    Given I request a banner
    Then I get an error for missing credentials

  @4.4
  Scenario: Request a new session with empty fields for *appid*, *userid*, *token*
    When I start the SDK
    Then I get an error for missing application id

  @4.5 @manual_check
  Scenario: I change userid and request banner again
    Given that I am a valid user with credentials
    When I request a banner
    And I wait for 5 seconds
    Then I see a banner
    And I take a picture
    Given that I am user "tester123" with valid credentials
    And I wait for 5 seconds
    Then I see a banner
    And I take a picture
    And I manually check both screenshots

  @4.6
  Scenario: Request offer banner
    Given that I am a valid user with credentials
    When I request a banner
    And I wait for 5 seconds
    Then I see a banner
    And the currency is "Sponsorpay"

  @4.7
  Scenario: I request banner with custom currency
    Given that I am a valid user with credentials
    And that custom currency is set to "bananas"
    When I request a banner
    * I wait for 5 seconds
    Then I see a banner
    And the currency is "bananas"

