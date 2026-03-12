# Script to download and setup winutils.exe for Spark on Windows
# This resolves the "Could not locate Hadoop executable" error

$ErrorActionPreference = "Stop"

Write-Host "Setting up winutils.exe for Spark..." -ForegroundColor Green

# Hadoop version used by Spark 3.5.0
$hadoopVersion = "3.3.4"
$winutilsUrl = "https://github.com/cdarlint/winutils/raw/master/hadoop-$hadoopVersion/bin/winutils.exe"
$hadoopDll = "https://github.com/cdarlint/winutils/raw/master/hadoop-$hadoopVersion/bin/hadoop.dll"

# Create directory structure
$hadoopHome = "$PSScriptRoot\spark-normalizer\tmp"
$binDir = "$hadoopHome\bin"

Write-Host "Creating directory: $binDir" -ForegroundColor Cyan
New-Item -ItemType Directory -Force -Path $binDir | Out-Null

# Download winutils.exe
Write-Host "Downloading winutils.exe from GitHub..." -ForegroundColor Cyan
try {
    Invoke-WebRequest -Uri $winutilsUrl -OutFile "$binDir\winutils.exe" -UseBasicParsing
    Write-Host "✓ winutils.exe downloaded successfully" -ForegroundColor Green
} catch {
    Write-Host "Error downloading winutils.exe: $_" -ForegroundColor Red
    exit 1
}

# Download hadoop.dll
Write-Host "Downloading hadoop.dll from GitHub..." -ForegroundColor Cyan
try {
    Invoke-WebRequest -Uri $hadoopDll -OutFile "$binDir\hadoop.dll" -UseBasicParsing
    Write-Host "✓ hadoop.dll downloaded successfully" -ForegroundColor Green
} catch {
    Write-Host "Warning: Could not download hadoop.dll: $_" -ForegroundColor Yellow
}

# Verify files
if (Test-Path "$binDir\winutils.exe") {
    $size = (Get-Item "$binDir\winutils.exe").Length / 1KB
    Write-Host "✓ winutils.exe ready (${size:N2} KB)" -ForegroundColor Green
} else {
    Write-Host "✗ Setup failed: winutils.exe not found" -ForegroundColor Red
    exit 1
}

Write-Host "`nSetup completed successfully!" -ForegroundColor Green
Write-Host "You can now run Spark Normalizer without winutils errors." -ForegroundColor Green
