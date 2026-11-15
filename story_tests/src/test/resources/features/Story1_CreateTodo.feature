@story1
Feature: Create a personal task
  As a user, I want to create a task so that I can remember what I need to do.

  Background: A responsive Todo Manager
    Given the Todo API is available

  @smoke
  Scenario Outline: Create a task with complete details (Normal Flow)
    When I create a todo using "JSON" with title "<title>" status "false" description "<description>"
    Then the HTTP response status is 201
    And the created todo contains title "<title>" status "false" description "<description>"

    Examples:
      | title                | description             |
      | Submit lab report    | Due before class        |
      | Review lecture notes | Prepare for examination |

  Scenario Outline: Create a task using another accepted representation (Alternate Flow)
    When I create a todo using "XML" with title "<title>" status "false" description "<description>"
    Then the HTTP response status is 201
    And the created todo contains title "<title>" status "false" description "<description>"

    Examples:
      | title             | description         |
      | Buy paper        | For course notes    |
      | Book study room  | For group revision  |

  Scenario Outline: Reject a task without a valid title (Error Flow)
    When I attempt to create a todo with invalid input "<invalidInput>"
    Then the HTTP response status is 400
    And the error response contains a meaningful message
    And the API collection is unchanged by the failed operation

    Examples:
      | invalidInput  |
      | missing-title |
      | empty-title   |
