@echo off
REM Run all tests for Network Monitoring System

echo ======================================
echo Running Tests - Network Monitoring System
echo ======================================
echo.

REM Run unit tests
echo Running unit tests...
call mvn clean test

if %errorlevel% neq 0 (
    echo ERROR: Unit tests failed!
    exit /b 1
)

echo ✓ Unit tests passed!
echo.

echo ======================================
echo All tests completed successfully!
echo ======================================
echo.

REM Generate test coverage report
echo Generating test coverage report...
call mvn verify

if %errorlevel% equ 0 (
    echo ✓ Coverage report generated!
    echo.
    echo Test reports available in:
    echo   - common\target\surefire-reports\
    echo   - producer-type-a\target\surefire-reports\
    echo   - producer-type-b\target\surefire-reports\
    echo   - spark-normalizer\target\surefire-reports\
    echo   - elasticsearch-consumer\target\surefire-reports\
) else (
    echo WARNING: Coverage report generation failed
)

echo.
pause
