@echo off
REM Script to download and setup winutils.exe for Spark on Windows

echo Setting up winutils.exe for Spark...

set HADOOP_VERSION=3.3.5
set WINUTILS_URL=https://github.com/cdarlint/winutils/raw/master/hadoop-%HADOOP_VERSION%/bin/winutils.exe
set HADOOP_DLL_URL=https://github.com/cdarlint/winutils/raw/master/hadoop-%HADOOP_VERSION%/bin/hadoop.dll

set HADOOP_HOME=%~dp0spark-normalizer\tmp
set BIN_DIR=%HADOOP_HOME%\bin

echo Creating directory: %BIN_DIR%
mkdir "%BIN_DIR%" 2>nul

echo Downloading winutils.exe from GitHub...
powershell -Command "Invoke-WebRequest -Uri '%WINUTILS_URL%' -OutFile '%BIN_DIR%\winutils.exe' -UseBasicParsing"
if %errorlevel% neq 0 (
    echo Error downloading winutils.exe
    exit /b 1
)
echo Downloaded winutils.exe successfully

echo Downloading hadoop.dll from GitHub...
powershell -Command "Invoke-WebRequest -Uri '%HADOOP_DLL_URL%' -OutFile '%BIN_DIR%\hadoop.dll' -UseBasicParsing"
if %errorlevel% neq 0 (
    echo Warning: Could not download hadoop.dll
)

echo.
echo Setup completed successfully!
echo You can now run Spark Normalizer without winutils errors.
pause
