
# ECSE 429 Software Validation — Parts A and B

## Overview

This repository contains the source code and supporting materials for Part A of ECSE 429 Software Validation. The project focuses on exploratory testing and automated unit testing of the supplied Todo Manager REST API.

The deliverables cover API capability demonstrations, exploratory-testing documentation, and a Java-based automated test suite using JUnit 5, Gradle, and REST Assured.

Part B extends the project through automated story testing of the same Todo Manager REST API. It uses Gherkin scenario outlines, Cucumber-JVM, and reusable Java step definitions to verify user stories through normal, alternate, and error flows.

## Repository Structure

- `capability_scripts/` — PowerShell scripts demonstrating API operations and behaviors explored during testing.
- `session_notes/` — Documentation for the three exploratory-testing sessions.
- `unit_tests/` — Gradle-based Java unit test suite, build configuration, Gradle Wrapper, JUnit random-order configuration, and test-specific instructions.
- `story_tests/` — Part B Cucumber story test suite, including Gherkin feature files, reusable Java step definitions, API utilities, and Gradle configuration.
- `bug_report_template.md` — Template for documenting potential defects and their reproduction steps.

## Prerequisites

- Java Development Kit (JDK) 21.
- The supplied Todo Manager REST API JAR, `runTodoManagerRestAPI-1.5.5.jar`.
- An internet connection for the initial Gradle dependency download.

Gradle does not need to be installed separately because the Gradle Wrapper is included in `unit_tests/`.

## Starting the API

1. Open PowerShell in the directory containing the supplied API JAR.
2. Start the application:

   ```powershell
   java -jar .\runTodoManagerRestAPI-1.5.5.jar
   ```

3. Keep the terminal open while testing. The API should be available at `http://localhost:4567`.

The application stores its data in memory. Restarting the JAR resets the data to its default state.

## Running the Automated Tests

With the API running, open a second PowerShell terminal in the repository root and execute:

```powershell
.\unit_tests\gradlew.bat -p .\unit_tests test --rerun-tasks
```

The command forces Gradle to rerun the test task. The JUnit configuration randomizes both test-class and test-method execution order.

The suite contains 35 tests across eight test classes. The verified successful execution produced 35 passed tests, zero failures, and zero skipped tests. Actual results may depend on the state and availability of the API.

For additional details about the test modules and test design, see `unit_tests/README.md`.

## Capability Scripts

The PowerShell scripts in `capability_scripts/` demonstrate the API operations and behaviors examined during exploratory testing, including:

- Retrieving Todos in JSON and XML.
- Creating Todos using JSON and XML.
- Updating and deleting Todos.
- Testing validation rules and input boundaries.
- Testing malformed payloads and nonexistent resources.
- Demonstrating the HEAD request.

Consult the README in `capability_scripts/` for script-specific usage instructions and safety considerations.

## Exploratory Testing

The `session_notes/` directory contains the documentation for three exploratory-testing sessions. These records describe the testing activities, observations, and behaviors examined during the project.

## Part B — Story Testing

Part B focuses on acceptance testing through five user stories related to the Todo Manager REST API.

The five stories cover:

1. **Create a Todo:** Create new tasks with valid details using JSON or XML and verify that invalid requests are rejected.
2. **Complete a Todo:** Mark an existing task as completed using POST or PUT and verify that invalid completion statuses are rejected.
3. **Edit a Todo:** Modify the title and description of an existing task using POST or PUT and verify that nonexistent tasks cannot be updated.
4. **Find a Todo:** Retrieve an existing task by ID or search for tasks using a title filter, including handling requests for nonexistent tasks.
5. **Delete a Todo:** Delete existing or completed tasks and verify that requests to delete nonexistent tasks are handled correctly.

Each user story includes normal, alternate, and error flows implemented as Gherkin Scenario Outlines with parameterized Examples tables.

The automated suite contains 15 scenario outlines and 30 executable scenarios across five feature files.

### Story Test Implementation

The story tests use Cucumber-JVM with Java and REST Assured to execute HTTP requests and verify API behavior.

Reusable step definitions implement common operations such as:

- Checking API availability.
- Creating temporary Todos.
- Retrieving, updating, searching, and deleting Todos.
- Verifying HTTP response codes and returned data.
- Checking error responses and unintended side effects.

Cucumber hooks manage test setup and cleanup. Before each scenario, the original Todo collection is recorded. After each scenario, temporary records are removed and the resulting collection is compared against the original snapshot.

This design allows scenarios to execute independently and in different orders without permanently modifying the API's existing data.

### Running the Story Tests

With the Todo Manager API running, open PowerShell in the repository root and execute:

```powershell
.\unit_tests\gradlew.bat -p .\story_tests cucumber -Pseed=12345 --rerun-tasks
```

To demonstrate execution in a different randomized order, run:

```powershell
.\unit_tests\gradlew.bat -p .\story_tests cucumber -Pseed=67890 --rerun-tasks
```

The `-Pseed` parameter controls the randomized scenario execution order, while `--rerun-tasks` forces Gradle to execute the tests again.

### Story Test Execution Results

The complete suite was executed successfully using two different random seeds.

| Execution | Scenarios Passed | Steps Passed | Failures |
|---|---|---|---|
| Seed 12345 | 30/30 | 150/150 | 0 |
| Seed 67890 | 30/30 | 150/150 | 0 |

Both executions completed successfully with different scenario orders.

Additional verification confirmed that:

- The test suite fails when the Todo Manager API is unavailable.
- Temporary Todos are removed after each scenario.
- The original Todo collection is restored after test execution.
- A manual PowerShell comparison of the API collection before and after a complete test run confirmed that the collection remained unchanged.

These results demonstrate successful story execution, test independence, service readiness verification, and restoration of the original system state.

## Additional Deliverables

The final written report and unit-test demonstration video for Part A are submitted separately through myCourses. The video demonstrates the test suite running in the selected development environment, including randomized execution order.

For Part B, the written report and story-test demonstration video are also submitted separately. The Part B report documents the user stories, test suite structure, implementation, source-code repository, and execution results. The video demonstrates the automated Cucumber scenarios running in different execution orders.
