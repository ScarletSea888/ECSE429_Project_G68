@story2
Feature: Mark a task as complete
  As a user, I want to mark completed tasks so that I can track my progress.

  Background: A responsive Todo Manager
    Given the Todo API is available

  Scenario Outline: Complete an unfinished task with POST (Normal Flow)
    Given a temporary todo exists with title "<title>" status "false" description "Pending task"
    When I set the todo status to "true" using "POST"
    Then the HTTP response status is 200
    And the current todo has status "true"

    Examples:
      | title             |
      | Finish assignment |
      | Read chapter 6    |

  Scenario Outline: Complete a task with PUT instead (Alternate Flow)
    Given a temporary todo exists with title "<title>" status "false" description "Pending task"
    When I set the todo status to "true" using "PUT"
    Then the HTTP response status is 200
    And the current todo has status "true"

    Examples:
      | title            |
      | Clean workspace  |
      | Update timetable |

  Scenario Outline: Reject a non-boolean completion status (Error Flow)
    Given a temporary todo exists with title "<title>" status "false" description "Pending task"
    When I try to set the todo status to invalid string "<invalidStatus>"
    Then the HTTP response status is 400
    And the error response contains a meaningful message
    And the current todo has status "false"
    And the API collection is unchanged by the failed operation

    Examples:
      | title               | invalidStatus |
      | Arrange meeting     | true          |
      | Check assignment    | completed     |
