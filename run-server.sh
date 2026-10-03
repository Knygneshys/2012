#!/bin/bash

# Bomberman Multiplayer - Spring Server Launcher
# Builds and runs the Spring Boot game server

set -e

echo "🎮 Bomberman Multiplayer - Spring Server Launcher"
echo "================================================="

PORT=${1:-8080}

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR/server"

echo "📦 Building server..."
mvn compile -q

echo ""
echo "🚀 Starting Bomberman Spring Server on WebSocket port $PORT"
echo "====================================================="
echo "⏳ Waiting for players to connect (2-4 players required)..."
echo "To stop the server, press Ctrl+C"
echo ""

mvn spring-boot:run -Dspring-boot.run.arguments="$PORT"
