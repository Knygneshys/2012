#!/bin/bash

# Bomberman Multiplayer - Client Launcher
# This script builds and runs the game client

set -e

echo "🎮 Bomberman Multiplayer - Client Launcher"
echo "==========================================="

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven is not installed. Installing Maven via Homebrew..."
    brew install maven
fi

HOST=${1:-localhost}
PORT=${2:-9876}

echo "📦 Building project..."
mvn clean package -q -DskipTests

echo ""
echo "🚀 Launching Bomberman Client"
echo "=============================="
echo "Connecting to: $HOST:$PORT"
echo ""

java -cp "target/classes:$(mvn dependency:build-classpath -q -Dmdep.outputFile=/dev/stdout)" com.example.MultiplayerApp $HOST $PORT

