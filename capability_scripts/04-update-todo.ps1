param([string]$TodoId = '5')
$BaseUrl = 'http://localhost:4567'
if ($TodoId -notmatch '^\d+$') { throw 'TodoId must be numeric.' }

$PostPayload = @{
    title = "Capability POST amendment $(Get-Date -Format 'yyyyMMdd-HHmmss')"
    doneStatus = $false
    description = 'Description changed using POST amendment.'
} | ConvertTo-Json
$PutPayload = @{
    title = "Capability PUT amendment $(Get-Date -Format 'yyyyMMdd-HHmmss')"
    doneStatus = $true
    description = 'Description changed using PUT amendment.'
} | ConvertTo-Json
$PostPath = Join-Path $env:TEMP 'ecse429-capability-amend-post.json'
$PutPath = Join-Path $env:TEMP 'ecse429-capability-amend-put.json'
$PostPayload | Set-Content -Encoding utf8 $PostPath
$PutPayload | Set-Content -Encoding utf8 $PutPath

Write-Warning "This modifies Todo $TodoId. Use a disposable test record."
Write-Host "=== POST /todos/$TodoId (amend) ==="
curl.exe -i -X POST "$BaseUrl/todos/$TodoId" -H 'Content-Type: application/json' -H 'Accept: application/json' --data-binary "@$PostPath"
Write-Host "`n=== PUT /todos/$TodoId (amend) ==="
curl.exe -i -X PUT "$BaseUrl/todos/$TodoId" -H 'Content-Type: application/json' -H 'Accept: application/json' --data-binary "@$PutPath"
Write-Host "`n=== GET /todos/$TodoId (verify current values) ==="
curl.exe -i "$BaseUrl/todos/$TodoId" -H 'Accept: application/json'
