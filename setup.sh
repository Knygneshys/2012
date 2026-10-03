#!/bin/bash

# Bomberman Multiplayer Setup and Quick Start Guide

set -e

echo "🎮 Bomberman Multiplayer - Setup"
echo "================================="
echo ""

# Check Java
echo "✓ Checking Java..."
if ! command -v javac &> /dev/null; then
    echo "❌ Java is not installed. Please install Java 17+"
    exit 1
fi
JAVA_VERSION=$(javac -version 2>&1 | awk '{print $2}')
echo "  Java version: $JAVA_VERSION"

# Check/Install Maven
echo ""
echo "✓ Checking Maven..."
if ! command -v mvn &> /dev/null; then
    echo "  Maven not found. Installing..."
    if command -v brew &> /dev/null; then
        brew install maven
    else
        echo "❌ Maven is not installed and brew not found."
        echo "   Please install Maven from https://maven.apache.org/download.cgi"
        exit 1
    fi
fi
MVN_VERSION=$(mvn --version 2>&1 | head -1)
echo "  $MVN_VERSION"

echo ""
echo "✓ Setup complete!"
echo ""
echo "=========================================="
echo "🚀 Quick Start Guide"
echo "=========================================="
echo ""
echo "1. START THE SERVER (in one terminal):"
echo "   ./run-server.sh"
echo ""
echo "2. START CLIENTS (in separate terminals, 2-4 players):"
echo "   ./run-client.sh"
echo ""
echo "3. PLAY!"
echo "   Controls:"
echo "   - WASD: Move"
echo "   - SPACE: Place bomb"
echo "   - R: Reset game"
echo ""
echo "For more info, see README.md"

