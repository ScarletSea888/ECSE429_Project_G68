$BaseUrl = 'http://localhost:4567'
$PayloadDir = Join-Path $PSScriptRoot 'payloads'

function Save-JsonPayload([string]$Name, $Object) {
    $Path = Join-Path $PayloadDir $Name
    $Object | ConvertTo-Json -Depth 5 | Set-Content -Encoding utf8 $Path
    return $Path
}
function Send-Post([string]$Label, [string]$Path) {
    Write-Host "`n=== $Label ==="
    curl.exe -i -X POST "$BaseUrl/todos" -H 'Content-Type: application/json' -H 'Accept: application/json' --data-binary "@$Path"
}

New-Item -ItemType Directory -Force -Path $PayloadDir | Out-Null
$Cases = @(
    @{ Name='missing-title.json'; Label='Missing title'; Data=@{ doneStatus=$false; description='Missing title test' } },
    @{ Name='empty-title.json'; Label='Empty title'; Data=@{ title=''; doneStatus=$false; description='Empty title test' } },
    @{ Name='whitespace-title.json'; Label='Whitespace-only title'; Data=@{ title='   '; doneStatus=$false; description='Whitespace title test' } },
    @{ Name='invalid-done-status.json'; Label='Invalid doneStatus string'; Data=@{ title='Invalid Boolean test'; doneStatus='maybe'; description='Invalid type test' } },
    @{ Name='client-id.json'; Label='Client-supplied ID'; Data=@{ id='999'; title='Client ID test'; doneStatus=$false; description='Should be rejected' } },
    @{ Name='unknown-field.json'; Label='Unknown field'; Data=@{ title='Unknown field test'; doneStatus=$false; description='Should be rejected'; unexpectedField='extra' } },
    @{ Name='special-title.json'; Label='Punctuation and Unicode title'; Data=@{ title=('ECSE429 capability: Hello! @#$%^&*() - caf' + [char]0x00E9); doneStatus=$false; description='Special character test' } }
)
foreach ($Case in $Cases) {
    $Path = Save-JsonPayload $Case.Name $Case.Data
    Send-Post $Case.Label $Path
}

$LongTitle = 'L' * 1000
$LongPath = Save-JsonPayload 'long-title-1000.json' @{ title=$LongTitle; doneStatus=$false; description='1000-character title boundary test' }
Send-Post '1000-character title' $LongPath

Write-Host "`nNote: invalid requests should be assessed by their responses; this script prints results but does not automatically judge pass/fail."
