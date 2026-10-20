$ErrorActionPreference = 'Stop'
$BaseUrl = 'http://localhost:4567'

Write-Host "`n=== GET all Todos (JSON) ==="
curl.exe -i "$BaseUrl/todos" -H "Accept: application/json"

Write-Host "`n=== GET all Todos (XML) ==="
curl.exe -i "$BaseUrl/todos" -H "Accept: application/xml"

$TodoId = Read-Host 'Enter an existing Todo ID to retrieve (e.g. 1)'
if ($TodoId -match '^\d+$') {
    Write-Host "`n=== GET Todo $TodoId (JSON) ==="
    curl.exe -i "$BaseUrl/todos/$TodoId" -H "Accept: application/json"
    Write-Host "`n=== GET Todo $TodoId (XML) ==="
    curl.exe -i "$BaseUrl/todos/$TodoId" -H "Accept: application/xml"
} else {
    Write-Warning 'No valid numeric ID supplied; skipped single-Todo requests.'
}
