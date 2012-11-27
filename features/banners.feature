Feature: Banners, production

  Background:
    Given that I am on production
    And that I am on the banners page

  Scenario: I request a banner with valid appid
    Given that I am user "tester" with valid credentials for "1246"
    When I request a banner
    * I wait for 5 seconds
    Then I see a banner

  Scenario: I request a banner with invalid appid
    Given that I am user "tester" with valid credentials for "1246a"
    When I request a banner
    * I wait for 5 seconds
    Then I see an error

  Scenario: I request a banner without credentials
    Given I request a banner
    Then I get an error for missing credentials

  Scenario: I change userid and request banner again
    Given that I am user "tester" with valid credentials for "1246"
    When I request a banner
    * I wait for 5 seconds
    Then I see a banner
    Given that I am user "tester123" with valid credentials for "1246"
    When I request a banner
    * I wait for 5 seconds
    Then I see a banner

  Scenario: I request banner with custom currency
    Given that I am user "tester" with valid credentials for "1246"
    And that custom currency is set to "bananas"
    When I request a banner
    * I wait for 5 seconds
    Then I see a banner
    And the currency is "bananas"

  Scenario: I touch the banner
    Given that I am user "tester" with valid credentials for "1246"
    When I request a banner
    * I wait for 5 seconds
    When I see a banner
    * I take a screenshot
    Then I touch the banner
    # not sure if this works
    * I wait for 5 seconds
    * I take a screenshot