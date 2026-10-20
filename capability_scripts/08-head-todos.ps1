$BaseUrl = 'http://localhost:4567'
Write-Host '=== HEAD /todos (headers only) ==='
curl.exe -I "$BaseUrl/todos"
