$BaseUrl = 'http://localhost:4567'
$Payload = @{
    title = "Capability JSON test $(Get-Date -Format 'yyyyMMdd-HHmmss')"
    doneStatus = $false
    description = 'Created by capability script; safe to delete after testing.'
} | ConvertTo-Json

$PayloadPath = Join-Path $env:TEMP 'ecse429-capability-create.json'
$Payload | Set-Content -Encoding utf8 $PayloadPath
Write-Host "Payload saved to $PayloadPath"
Write-Host '=== POST /todos (valid JSON) ==='
curl.exe -i -X POST "$BaseUrl/todos" -H 'Content-Type: application/json' -H 'Accept: application/json' --data-binary "@$PayloadPath"
