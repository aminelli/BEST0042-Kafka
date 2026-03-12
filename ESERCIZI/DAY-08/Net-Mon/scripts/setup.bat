@echo off
REM Network Monitoring System - Setup Script for Windows

echo ================================
echo Network Monitoring System - Setup
echo ================================
echo.

REM Check prerequisites
echo Checking prerequisites...

where docker >nul 2>nul
if %errorlevel% neq 0 (
    echo ERROR: Docker is required but not installed.
    exit /b 1
)

where docker-compose >nul 2>nul
if %errorlevel% neq 0 (
    echo ERROR: Docker Compose is required but not installed.
    exit /b 1
)

where mvn >nul 2>nul
if %errorlevel% neq 0 (
    echo ERROR: Maven is required but not installed.
    exit /b 1
)

echo ✓ Docker found
echo ✓ Docker Compose found
echo ✓ Maven found
echo.

REM Build the project
echo Building the project with Maven...
call mvn clean package -DskipTests

if %errorlevel% neq 0 (
    echo ERROR: Build failed. Please check the errors above.
    exit /b 1
)

echo ✓ Build successful!
echo.

REM Create necessary directories
echo Creating necessary directories...
if not exist logs mkdir logs
if not exist checkpoint mkdir checkpoint
echo ✓ Directories created!
echo.

REM Pull Docker images
echo Pulling Docker images...
docker-compose pull
echo.

REM Start infrastructure services
echo Starting infrastructure services (Kafka, Elasticsearch, Kibana)...
docker-compose up -d zookeeper kafka elasticsearch kibana
echo.

echo Waiting for services to be ready (30 seconds)...
timeout /t 30 /nobreak >nul
echo.

REM Start application services
echo Starting application services...
docker-compose up -d
echo.

echo ================================
echo Setup completed successfully!
echo ================================
echo.
echo Access Points:
echo   - Kibana: http://localhost:5601
echo   - Elasticsearch: http://localhost:9200
echo.
echo Useful commands:
echo   - View logs: docker-compose logs -f
echo   - Stop all: docker-compose down
echo   - Restart: docker-compose restart
echo.
echo Next steps:
echo   1. Open Kibana at http://localhost:5601
echo   2. Create index pattern: network-telemetry-*
echo   3. Go to Discover to view telemetry data
echo.

pause
