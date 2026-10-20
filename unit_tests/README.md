# ECSE 429 Part A — Todo API Unit Tests

## Overview

This project contains a JUnit 5 test suite for the Todo Manager REST API supplied for ECSE 429 Software Validation, Part A.

The tests use Java, Gradle, and REST Assured to verify API responses, input validation, persistence of updates, and data integrity. The scope is limited to the Todo API.

## Prerequisites

- Java Development Kit (JDK) 21
- The supplied Todo Manager API JAR
- A network connection for the first Gradle build to download dependencies

The Gradle Wrapper is included so that Gradle does not need to be installed separately.

## Starting the API

1. Open a terminal in the directory containing `runTodoManagerRestAPI-1.5.5.jar`.
2. Run:

   ```powershell
   java -jar .\runTodoManagerRestAPI-1.5.5.jar
   ```

3. Keep that terminal open while running the tests. The API should be available at `http://localhost:4567`.

The application stores data in memory. Restarting the JAR resets its data to the application's default state.

## Running the tests

Open a separate PowerShell terminal in this directory (unit_tests) and run:

```powershell
.\gradlew.bat test --rerun-tasks
```

The `--rerun-tasks` option forces Gradle to execute the tests again instead of reusing an up-to-date test task result.

JUnit randomizes test-class and test-method order through `src/test/resources/junit-platform.properties`. The test output lists individual test results and a summary.

## Test modules

| Test module | Main coverage |
|---|---|
| `TodoCollectionApiTest` | Retrieve the Todo collection in JSON and XML |
| `TodoCollectionBehaviorApiTest` | Filtering, sorting behavior, HEAD, OPTIONS, and unsupported PATCH |
| `TodoItemApiTest` | Retrieve individual Todos in JSON and XML; nonexistent Todo |
| `TodoCreateApiTest` | Valid JSON/XML creation and invalid title requests |
| `TodoUpdateApiTest` | POST and PUT updates, persistence, and nonexistent resources |
| `TodoDeleteApiTest` | Deletion, retrieval after deletion, and repeated deletion |
| `TodoValidationApiTest` | Invalid fields and types, malformed payloads, and title boundaries |
| `TodoSideEffectsApiTest` | Rejected creation and unintended changes to other Todos |

## Test design

The tests check HTTP status codes and, where relevant, response content, returned fields, and whether changes persist. Tests create temporary Todos where needed and remove them during cleanup. Readiness checks help detect when the API is unavailable.

Randomized ordering is used to help identify tests that depend on other tests having run first.

## Related deliverables

The exploratory testing session notes, capability scripts, and written report are maintained separately in the Part A submission directory.
