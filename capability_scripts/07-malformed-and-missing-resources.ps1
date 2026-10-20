$BaseUrl = 'http://localhost:4567'
$PayloadDir = Join-Path $PSScriptRoot 'payloads'
New-Item -ItemType Directory -Force -Path $PayloadDir | Out-Null

$BadJsonPath = Join-Path $PayloadDir 'malformed.json'
@'
{
  "title": "Malformed JSON test"
  "doneStatus": false,
  "description": "Missing comma after title"
}
'@ | Set-Content -Encoding utf8 $BadJsonPath

$BadXmlPath = Join-Path $PayloadDir 'malformed.xml'
@'
<todo>
  <title>Malformed XML test
  <doneStatus>false</doneStatus>
  <description>Missing closing title tag</description>
</todo>
'@ | Set-Content -Encoding utf8 $BadXmlPath

Write-Host '=== POST malformed JSON ==='
curl.exe -i -X POST "$BaseUrl/todos" -H 'Content-Type: application/json' --data-binary "@$BadJsonPath"
Write-Host "`n=== POST malformed XML ==="
curl.exe -i -X POST "$BaseUrl/todos" -H 'Content-Type: application/xml' --data-binary "@$BadXmlPath"

$MissingId = '99999'
Write-Host "`n=== GET nonexistent Todo ==="
curl.exe -i "$BaseUrl/todos/$MissingId"
Write-Host "`n=== PUT nonexistent Todo ==="
$PutPath = Join-Path $env:TEMP 'ecse429-capability-missing-put.json'
@{ title='Nonexistent update'; doneStatus=$false; description='Should not create this record' } | ConvertTo-Json | Set-Content -Encoding utf8 $PutPath
curl.exe -i -X PUT "$BaseUrl/todos/$MissingId" -H 'Content-Type: application/json' --data-binary "@$PutPath"
Write-Host "`n=== DELETE nonexistent Todo ==="
curl.exe -i -X DELETE "$BaseUrl/todos/$MissingId"
