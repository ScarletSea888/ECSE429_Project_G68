# ECSE 429 Software Validation — Part A

## Overview

This repository contains the source code and supporting materials for Part A of ECSE 429 Software Validation. The project focuses on exploratory testing and automated unit testing of the supplied Todo Manager REST API.

The deliverables cover API capability demonstrations, exploratory-testing documentation, and a Java-based automated test suite using JUnit 5, Gradle, and REST Assured.

## Repository Structure

- `capability_scripts/` — PowerShell scripts demonstrating API operations and behaviors explored during testing.
- `session_notes/` — Documentation for the three exploratory-testing sessions.
- `unit_tests/` — Gradle-based Java unit test suite, build configuration, Gradle Wrapper, JUnit random-order configuration, and test-specific instructions.
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

## Additional Deliverables

The final written report and unit-test demonstration video are submitted separately through the course submission process. The video demonstrates the test suite running in the selected development environment, including randomized execution order.

