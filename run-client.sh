#!/bin/bash

# Bomberman Multiplayer - Client Launcher
# Builds and runs the Swing game client

set -e

echo "🎮 Bomberman Multiplayer - Swing Client Launcher"
echo "================================================="

HOST=${1:-localhost}
PORT=${2:-8080}

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR/client"

echo "📦 Building client..."
mvn compile -q

echo ""
echo "🚀 Launching Bomberman Swing Client"
echo "===================================="
echo "Connecting to: $HOST:$PORT"
echo ""

mvn exec:java -Dexec.mainClass="com.example.client.MultiplayerApp" -Dexec.args="$HOST $PORT"
