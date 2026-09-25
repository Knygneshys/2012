#!/usr/bin/env mdcat

# 🎮 Bomberman Multiplayer - Quick Reference

## Installation (First Time Only)

```bash
# 1. Install dependencies
brew install java maven  # macOS
# or use your system package manager

# 2. Clone/navigate to project
cd 2012-bomberman

# 3. Verify setup
./setup.sh
```

## Playing Locally (Same Machine)

```bash
# Terminal 1 - Start Server
./run-server.sh

# Terminal 2 - Start Client 1
./run-client.sh

# Terminal 3 - Start Client 2
./run-client.sh

# (Optional) Terminal 4 - Start Client 3
./run-client.sh

# (Optional) Terminal 5 - Start Client 4
./run-client.sh
```

## Playing Over Network

```bash
# On SERVER machine
./run-server.sh

# On CLIENT machines (replace 192.168.1.100 with server IP)
./run-client.sh 192.168.1.100
```

## Game Controls

| Key | Action |
|-----|--------|
| **W** | Move Up |
| **S** | Move Down |
| **A** | Move Left |
| **D** | Move Right |
| **SPACE** | Place Bomb |
| **R** | Reset Game |

## Game Rules

- **2-4 Players** required to start
- **Goal**: Be the last player standing
- **Map**: 13×11 grid with walls and blocks
- **Bombs**: Explode after ~2 seconds, radius of 2 tiles
- **Destroyed**: Blocks and touching explosions
- **Win**: All other players eliminated

## Player Colors

| Position | Color |
|----------|-------|
| Top-Left | BLUE |
| Top-Right | RED |
| Bottom-Left | YELLOW |
| Bottom-Right | GREEN |

## Troubleshooting Quick Fixes

| Problem | Solution |
|---------|----------|
| Connection refused | Check server is running, use correct IP |
| Game is full | Max 4 players, start new server on different port |
| Slow/Laggy | Check network, close other apps, wired connection better |
| Java not found | `brew install openjdk@17` (macOS) |
| Maven not found | `brew install maven` (macOS) |
| Bombs don't show | Wait 1-2 seconds for sync, try moving |

## Advanced Usage

```bash
# Custom port (default: 9876)
./run-server.sh 5555
./run-client.sh localhost 5555

# Remote server
./run-client.sh 192.168.1.100 9876

# Manual build
mvn clean package

# Debug output
DEBUG=true ./run-server.sh
```

## File Locations

```
2012-bomberman/
├── run-server.sh          ← Start server
├── run-client.sh          ← Start client
├── setup.sh               ← Verify setup
├── README.md              ← Full documentation
├── MULTIPLAYER_GUIDE.md   ← Detailed guide
├── pom.xml                ← Maven config
└── src/main/java/
    └── com/example/
        ├── MultiplayerApp.java      ← Client launcher
        ├── GameController.java      ← Input handling
        ├── MapPanel.java            ← Game rendering
        ├── server/
        │   └── GameServer.java      ← Server (CLI)
        ├── client/
        │   └── NetworkClient.java   ← Network handling
        └── network/
            ├── GameMessage.java
            ├── GameStateMessage.java
            ├── PlayerInputMessage.java
            └── GameStateData.java
```

## Network Ports

- **Default Port**: 9876
- **Check if in use**: `lsof -i :9876` (macOS/Linux)
- **Alternative ports**: Any 1024-65535 not in use

## Tips for Good Gameplay

1. **Positioning** - Stay in open areas when placing bombs
2. **Strategy** - Use walls to block opponents
3. **Timing** - Wait for opponent to get close before bombing
4. **Exploration** - Break blocks to find better routes
5. **Dodging** - Always have an escape route from your bomb

## Performance Tips

- **LAN**: ~50-100ms latency (excellent)
- **WAN**: Requires low-latency network or VPN
- **Bandwidth**: ~10 KB/s (minimal)
- **WiFi**: Use 5GHz for better performance
- **Updates**: Server 10x/sec, Client 60 FPS

## Common Issues & Fixes

### Can't connect to server
```bash
# Test network connectivity
ping 192.168.1.100

# Check if port is open
netstat -an | grep 9876

# Try different port
./run-server.sh 5555
./run-client.sh 192.168.1.100 5555
```

### Game freezes
```bash
# Restart both server and clients
# Check for network issues
ping -c 5 <server-ip>
```

### Build fails
```bash
# Clean rebuild
rm -rf target/
mvn clean install
mvn clean package
```

## Getting More Help

- `README.md` - Full feature documentation
- `MULTIPLAYER_GUIDE.md` - Detailed setup instructions
- Server console - Prints connection info and game events
- Check all `.java` files in `src/main/java/com/example/`

---

**Version**: Bomberman Multiplayer Edition  
**Java**: 17+  
**Last Updated**: 2024

