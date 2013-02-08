Feature: Rewarded actions, production

  Background:
    Given that I am on production
    And that I am on the rewarded actions page

  @7.1
  Scenario: Report valid live action
    Given that I am a valid user with credentials
    When I report "ACTION_1" action as completed
    Then the log ends with "Server returned status code: 200" 

  @7.2
  Scenario: Report non existing action
    Given that I am a valid user with credentials
    When I report "UNEXISTING_BUT_VALID_ACTION_ID" action as completed
    Then the log ends with "Server returned status code: 200"

  @7.3
  Scenario: Report invalid action
    Given that I am a valid user with credentials
    When I report "invalid_id" action as completed
    Then I get an error for invalid action id

  @7.4
  Scenario: Report action empty field
    Given that I am a valid user with credentials
    And that I use empty action id
    When I report completed action
    Then I get an error for empty action id

  @7.5
  Scenario: Report valid action without a session created
    When I report "VALID_ACTION_ID" action as completed
    Then I get an error for missing credentials

  @7.6
  Scenario: Report two actions of the same program
    Given that I am a valid user with credentials
    When I report "ACTION_1" action as completed
    Then the log ends with "Server returned status code: 200"
    When I report "ACTION_2" action as completed
    Then the log ends with "Server returned status code: 200"