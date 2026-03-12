# PowerShell script to run tests
Set-Location "d:\Temp\Kafka\UCASE\Net-Mon"
Write-Host "======================================"
Write-Host "Running Tests - Network Monitoring System"
Write-Host "======================================"
Write-Host ""

Write-Host "Running unit tests..." -ForegroundColor Yellow
& mvn clean test -q

if ($LASTEXITCODE -eq 0) {
    Write-Host "✓ Unit tests passed!" -ForegroundColor Green
} else {
    Write-Host "✗ Unit tests failed!" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "======================================"
Write-Host "✓ All tests completed successfully!"
Write-Host "======================================"
