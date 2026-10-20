param([Parameter(Mandatory=$true)][string]$TodoId)
$BaseUrl = 'http://localhost:4567'
if ($TodoId -notmatch '^\d+$') { throw 'TodoId must be numeric.' }
Write-Warning "This permanently deletes Todo $TodoId from the current in-memory API state."
$ConfirmDelete = Read-Host "Type DELETE to continue"
if ($ConfirmDelete -ne 'DELETE') { Write-Host 'Cancelled.'; exit }
Write-Host "=== DELETE /todos/$TodoId ==="
curl.exe -i -X DELETE "$BaseUrl/todos/$TodoId"
Write-Host "`n=== GET /todos/$TodoId (verify deletion) ==="
curl.exe -i "$BaseUrl/todos/$TodoId"
