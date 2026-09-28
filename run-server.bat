@echo off
set PORT=%1
if "%PORT%"=="" set PORT=8080

echo [Server] Starting Bomberman Spring Server on WebSocket port %PORT%...
cd /d "%~dp0server"
mvn spring-boot:run -Dspring-boot.run.arguments="%PORT%"
