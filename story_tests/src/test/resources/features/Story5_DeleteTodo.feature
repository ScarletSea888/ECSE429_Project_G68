@story5
Feature: Remove unnecessary tasks
  As a user, I want to delete tasks I no longer need so that my task list stays relevant.

  Background: A responsive Todo Manager
    Given the Todo API is available

  Scenario Outline: Delete an existing task (Normal Flow)
    Given a temporary todo exists with title "<title>" status "false" description "Temporary task"
    When I delete the current todo
    Then the HTTP response status is 200
    And the current todo no longer exists

    Examples:
      | title               |
      | Discard old note    |
      | Cancel draft email  |

  Scenario Outline: Delete a completed task (Alternate Flow)
    Given a temporary todo exists with title "<title>" status "false" description "Completed task"
    When I mark the current todo completed and then delete it
    Then the HTTP response status is 200
    And the current todo no longer exists

    Examples:
      | title               |
      | Archive homework    |
      | Remove finished job |

  Scenario Outline: Reject deletion of an unknown task (Error Flow)
    When I delete nonexistent todo "<id>"
    Then the HTTP response status is 404
    And the error response contains a meaningful message
    And the API collection is unchanged by the failed operation

    Examples:
      | id        |
      | 999999995 |
      | 999999994 |
