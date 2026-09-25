#!/bin/bash

# Bomberman Multiplayer - Server Launcher
# This script builds and runs the game server

set -e

echo "🎮 Bomberman Multiplayer - Server Launcher"
echo "=========================================="

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven is not installed. Installing Maven via Homebrew..."
    brew install maven
fi

PORT=${1:-9876}

echo "📦 Building project..."
mvn clean package -q -DskipTests

echo ""
echo "🚀 Starting Bomberman Server on port $PORT"
echo "=========================================="
echo "⏳ Waiting for players to connect (2-4 players required)..."
echo ""
echo "To stop the server, press Ctrl+C"
echo ""

java -cp "target/classes:$(mvn dependency:build-classpath -q -Dmdep.outputFile=/dev/stdout)" com.example.server.GameServer $PORT

