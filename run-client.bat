@echo off
set HOST=%1
if "%HOST%"=="" set HOST=localhost

set PORT=%2
if "%PORT%"=="" set PORT=8080

echo [Client] Starting Bomberman Swing Client connecting to %HOST%:%PORT%...
cd /d "%~dp0client"
mvn exec:java -Dexec.mainClass="com.example.client.MultiplayerApp" -Dexec.args="%HOST% %PORT%"
