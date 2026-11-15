@story3
Feature: Edit a task description and title
  As a user, I want to change task details so that my task list remains accurate.

  Background: A responsive Todo Manager
    Given the Todo API is available

  Scenario Outline: Edit task details with POST (Normal Flow)
    Given a temporary todo exists with title "Original task" status "false" description "Old description"
    When I change the todo title to "<title>" and description to "<description>" using "POST"
    Then the HTTP response status is 200
    And the current todo has title "<title>" and description "<description>"

    Examples:
      | title                 | description         |
      | Revise project plan   | Review milestones   |
      | Submit revised report | Upload final PDF    |

  Scenario Outline: Edit task details with PUT (Alternate Flow)
    Given a temporary todo exists with title "Original task" status "false" description "Old description"
    When I change the todo title to "<title>" and description to "<description>" using "PUT"
    Then the HTTP response status is 200
    And the current todo has title "<title>" and description "<description>"

    Examples:
      | title              | description        |
      | Read updated notes | New chapter notes  |
      | File the receipts  | For reimbursement  |

  Scenario Outline: Reject edits to an unknown task (Error Flow)
    When I change nonexistent todo "<id>" using "<method>"
    Then the HTTP response status is 404
    And the error response contains a meaningful message
    And the API collection is unchanged by the failed operation

    Examples:
      | id        | method |
      | 999999998 | POST   |
      | 999999999 | PUT    |
