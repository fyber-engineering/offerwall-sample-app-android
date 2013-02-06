Feature: Rewarded actions, production

  Background:
    Given that I am on production
    And that I am on the rewarded actions page

  Scenario: Report valid live action
    Given that I am user "testing_actions" with valid credentials for "10876"
    When I report "ACTION_1" action as completed
    # check log here

  Scenario: Report non existing action
    Given that I am user "testing_actions" with valid credentials for "10876"
    When I report "UNEXISTING_BUT_VALID_ACTION_ID" action as completed
    # check log here

  Scenario: Report invalid action
    Given that I am user "testing_actions" with valid credentials for "10876"
    When I report "invalid_id" action as completed
    Then I get an error for invalid action id

  Scenario: Report action empty field
    Given that I am user "testing_actions" with valid credentials for "10876"
    And that I use empty action id
    When I report completed action
    Then I get an error for empty action id

  Scenario: Report valid action without a session created
    When I report "VALID_ACTION_ID" action as completed
    Then I get an error for missing credentials

  Scenario: Report two actions of the same program
    Given that I am user "testing_actions" with valid credentials for "10876"
    When I report "ACTION_1" action as completed
    # check log here
    When I report "ACTION_2" action as completed
    # check log here