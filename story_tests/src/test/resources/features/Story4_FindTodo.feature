@story4
Feature: Find a task to review progress
  As a user, I want to locate my tasks so that I can review what needs attention.

  Background: A responsive Todo Manager
    Given the Todo API is available

  Scenario Outline: Retrieve an existing task by ID (Normal Flow)
    Given a temporary todo exists with title "<title>" status "false" description "Find by ID"
    When I retrieve the current todo by ID
    Then the HTTP response status is 200
    And the retrieved todo has title "<title>" and status "false"

    Examples:
      | title              |
      | Review unit tests  |
      | Prepare for quiz   |

  Scenario Outline: Find a task using an exact title filter (Alternate Flow)
    Given a temporary todo exists with title "<title>" status "false" description "Find by title"
    When I search todos with exact title "<title>"
    Then the HTTP response status is 200
    And the search results include a todo titled "<title>"

    Examples:
      | title               |
      | Write introduction  |
      | Collect references  |

  Scenario Outline: Request a task with an unknown ID (Error Flow)
    When I retrieve nonexistent todo "<id>"
    Then the HTTP response status is 404
    And the error response contains a meaningful message
    And the API collection is unchanged by the failed operation

    Examples:
      | id        |
      | 999999997 |
      | 999999996 |
