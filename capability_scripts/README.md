# ECSE 429 Part A — Capability Scripts

These PowerShell scripts reproduce the main Todo API capabilities explored during the exploratory testing sessions. They are **demonstration scripts**, not a pass/fail automated unit-test suite.

## Requirements

- Windows PowerShell or PowerShell 7
- Java Todo Manager API running separately at `http://localhost:4567`
- `curl.exe` available on PATH

## How to Run

Open a PowerShell terminal in this folder, start the API JAR in a separate terminal, then run a script. For example:

```powershell
.\01-read-todos.ps1
```

If script execution policy blocks local scripts, you can run scripts for the current terminal session with:

```powershell
Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass
```

## Scripts

1. `01-read-todos.ps1` — GET all Todos as JSON/XML and GET one Todo.
2. `02-create-todo-json.ps1` — Create a Todo with valid JSON.
3. `03-create-todo-xml.ps1` — Create a Todo with valid XML.
4. `04-update-todo.ps1` — Perform POST and PUT amendments. Accepts an optional `-TodoId` parameter, which defaults to `5`. Example:

   ```powershell
   .\04-update-todo.ps1 -TodoId 6
   ```

5. `05-delete-todo.ps1` — Delete an existing test Todo. The `-TodoId` parameter is mandatory, and the script asks for confirmation before deleting the record. Example:

   ```powershell
   .\05-delete-todo.ps1 -TodoId 5
   ```

6. `06-validation-and-boundaries.ps1` — Test missing titles, empty and whitespace-only titles, invalid Boolean values, client-supplied IDs, unknown fields, long titles, and special characters.
7. `07-malformed-and-missing-resources.ps1` — Test malformed JSON/XML and GET, PUT, and DELETE requests for a nonexistent ID.
8. `08-head-todos.ps1` — Demonstrate a HEAD request.

## Notes and Safety

- Scripts that create records can change the API's in-memory state. Restarting the JAR may reset that state.
- The create scripts print the server response and do not assume a fixed new Todo ID.
- The update and delete scripts target an existing ID. Use a disposable test Todo rather than a record you want to keep. The default update ID `5` is the sample test Todo from the recorded session; change it if your API state differs.
- The delete script requires explicit confirmation before proceeding.
- Validation scripts intentionally submit invalid requests. These requests are expected to produce errors, and the scripts are not designed to stop on non-2xx HTTP status codes.
- These scripts are based on the experiments documented so far. Adjust them if the course rubric requires a particular file format or script organization.