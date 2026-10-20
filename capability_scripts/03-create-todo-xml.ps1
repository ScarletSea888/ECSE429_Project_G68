$BaseUrl = 'http://localhost:4567'
$Stamp = Get-Date -Format 'yyyyMMdd-HHmmss'
$Xml = @"
<todo>
  <title>Capability XML test $Stamp</title>
  <doneStatus>false</doneStatus>
  <description>Created by capability script; safe to delete after testing.</description>
</todo>
"@
$PayloadPath = Join-Path $env:TEMP 'ecse429-capability-create.xml'
$Xml | Set-Content -Encoding utf8 $PayloadPath
Write-Host "Payload saved to $PayloadPath"
Write-Host '=== POST /todos (valid XML) ==='
curl.exe -i -X POST "$BaseUrl/todos" -H 'Content-Type: application/xml' -H 'Accept: application/xml' --data-binary "@$PayloadPath"
