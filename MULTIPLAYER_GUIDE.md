# Bomberman Multiplayer Guide

This guide provides detailed instructions for setting up and playing multiplayer Bomberman.

## System Requirements

- **Operating System**: macOS, Linux, or Windows (with WSL)
- **Java**: Java 17 or higher
- **Maven**: 3.6 or higher
- **Network**: Two computers should be able to reach each other (LAN or VPN)

## Installation

### Step 1: Install Java

**macOS (Homebrew):**
```bash
brew install openjdk@17
```

**Linux (Ubuntu/Debian):**
```bash
sudo apt-get install openjdk-17-jdk
```

**macOS/Linux (Manual):**
Download from [java.com](https://www.java.com/download/ie_manual.jsp)

**Windows:**
Download from [java.com](https://www.java.com/download/ie_manual.jsp)

### Step 2: Install Maven

**macOS (Homebrew):**
```bash
brew install maven
```

**Linux (Ubuntu/Debian):**
```bash
sudo apt-get install maven
```

**Manual (All platforms):**
1. Download Apache Maven from [maven.apache.org](https://maven.apache.org/download.cgi)
2. Extract to a folder (e.g., `~/apache-maven-3.9.16`)
3. Add to PATH:
   ```bash
   export PATH="/path/to/apache-maven-3.9.16/bin:$PATH"
   ```

### Step 3: Get Bomberman

```bash
git clone <repository-url>
cd 2012-bomberman
```

### Step 4: Run Setup

```bash
./setup.sh
```

This will verify your Java and Maven installation.

## Playing Locally (on same machine)

### 1. Start Server
```bash
./run-server.sh
```

Output should show:
```
🎮 Bomberman Server started on port 9876
⏳ Waiting for players to connect (2-4 players required)...
```

### 2. Start Clients (in new terminals)

**Client 1:**
```bash
./run-client.sh localhost 9876
```

**Client 2:**
```bash
./run-client.sh localhost 9876
```

**Client 3 (optional):**
```bash
./run-client.sh localhost 9876
```

**Client 4 (optional):**
```bash
./run-client.sh localhost 9876
```

### 3. Connect

In each client window, a connection dialog appears. Enter a unique player name and click "Connect".

### 4. Play!

Once 2+ players are connected, the game starts automatically.

## Playing Over Network

### Prerequisites
- Server and clients must be on the same network
- Or use a VPN if not on the same network
- Server must not be behind a firewall blocking port 9876 (or use a different port)

### 1. Find Server's IP Address

**On the server machine:**

**macOS/Linux:**
```bash
ifconfig | grep "inet " | grep -v 127.0.0.1
```

Example output:
```
inet 192.168.1.100 netmask 0xffffff00 broadcast 192.168.1.255
```
Note: `192.168.1.100` is the IP address.

**Windows (WSL):**
```bash
ipconfig
```
Look for IPv4 Address (e.g., `192.168.1.100`)

### 2. Start Server

On the server machine:
```bash
./run-server.sh 9876
```

### 3. Start Clients on Other Machines

On each client machine:
```bash
./run-client.sh 192.168.1.100 9876
```

Replace `192.168.1.100` with the actual server IP.

### 4. Enter Player Names

In each client, enter a unique player name and click "Connect".

### 5. Play!

## Advanced Configuration

### Custom Port

To use a different port (if 9876 is already in use):

**Start server on port 5555:**
```bash
./run-server.sh 5555
```

**Connect clients:**
```bash
./run-client.sh localhost 5555
```

### Building Manually

```bash
# Clean build
mvn clean package

# Run server
java -cp "target/classes:$(mvn dependency:build-classpath -q -Dmdep.outputFile=/dev/stdout)" \
  com.example.server.GameServer 9876

# Run client
java -cp "target/classes:$(mvn dependency:build-classpath -q -Dmdep.outputFile=/dev/stdout)" \
  com.example.MultiplayerApp localhost 9876
```

### Debugging

Enable debug output:
```bash
# Add to run commands
export DEBUG=true
./run-server.sh
```

## Troubleshooting

### "Connection refused"

**Problem:** Client cannot connect to server

**Solutions:**
1. Ensure server is running: `./run-server.sh`
2. Check correct IP/hostname: Use `ifconfig` to find server IP
3. Check firewall: Disable firewall or add exception for port 9876
4. Check network: Ping server from client
   ```bash
   ping 192.168.1.100
   ```

### "Game is full" message

**Problem:** You see this when trying to join

**Solution:** The server already has 4 players. Wait for a game to finish or start a new server on a different port.

### Maven not found

**Problem:** `command not found: mvn`

**Solutions:**
1. Install Maven:
   ```bash
   brew install maven  # macOS
   sudo apt-get install maven  # Linux
   ```
2. Add to PATH:
   ```bash
   export PATH="/usr/local/opt/maven/bin:$PATH"
   echo 'export PATH="/usr/local/opt/maven/bin:$PATH"' >> ~/.zshrc
   ```
3. Verify:
   ```bash
   mvn --version
   ```

### Java not found

**Problem:** `command not found: java` or `command not found: javac`

**Solutions:**
1. Install Java 17:
   ```bash
   brew install openjdk@17  # macOS
   sudo apt-get install openjdk-17-jdk  # Linux
   ```
2. Add to PATH:
   ```bash
   export PATH="/Library/Java/JavaVirtualMachines/openjdk-17.jdk/Contents/Home/bin:$PATH"
   ```

### Slow/Laggy Game

**Problem:** Game feels slow or delayed

**Solutions:**
1. Check network: Run `ping` to server
2. Close other applications using network
3. Move closer to router (WiFi)
4. Use wired connection if possible
5. Reduce other network traffic

### Players can't see each other

**Problem:** You see your player but not others

**Solutions:**
1. Ensure all players connected (check server output)
2. Wait 1-2 seconds for sync
3. Try moving - sometimes display updates on movement
4. Restart clients if stuck

### Bomb doesn't explode

**Problem:** You placed a bomb but it didn't explode

**Solution:** This is normal - bombs take ~2 seconds to explode. Move away to stay safe!

## Tips & Tricks

### Playing Tips
- Place bombs strategically to trap opponents
- Destroy soft blocks to find items
- Use walls to block bomb blasts
- Avoid dead ends if opponents are nearby

### Network Tips
- Use VPN for remote play
- Ensure sufficient bandwidth
- Close background applications
- Use 5GHz WiFi for better latency if available

### Common Strategies
- **Defensive**: Build walls with destroyed blocks
- **Aggressive**: Push opponents into corners
- **Explorer**: Break blocks to find better paths
- **Explosive**: Chain bombs for area denial

## Getting Help

If you encounter issues:

1. Check this troubleshooting guide
2. Review server output for error messages
3. Try with both players on same machine first
4. Check network connectivity with `ping`
5. Try a different port (e.g., 5555)

## Architecture Overview

```
┌─────────────────┐
│  Game Server    │
│  (port 9876)    │
│  - Game Logic   │
│  - State Sync   │
│  - Physics      │
└────────┬────────┘
         │
    ┌────┼────┐
    │    │    │
┌───┴──┐ ┌──┴───┐ ┌────────┐
│Client│ │Client│ │ Client │
│  1   │ │  2   │ │   3    │
└──────┘ └──────┘ └────────┘
```

## Network Protocol

Messages sent as JSON over TCP:

```
Client → Server: {"messageType":"PLAYER_JOIN","playerName":"Alice"}
Server → Client: {"messageType":"SERVER_RESPONSE","success":true,"message":"...","playerId":1}
Client → Server: {"messageType":"PLAYER_INPUT","playerId":1,"action":"UP"}
Server → Client: {"messageType":"GAME_STATE","gameState":{...}} (10x/sec)
```

## Performance Notes

- Server updates game state 10 times/second
- Clients render at ~60 FPS
- Network latency: 50-100ms typical local
- Minimum bandwidth: ~10 KB/s
- Works well on LAN, requires low-latency network for WAN

## Future Features

- Chat system
- Spectator mode
- Multiple game rooms
- Power-ups
- Custom maps
- Replay system
- Statistics tracking

## Version Info

- **Bomberman**: Multiplayer Edition
- **Java Target**: 17+
- **Network**: TCP/JSON
- **Max Players**: 4
- **Game Loop**: 100ms/tick (10 FPS server, 60 FPS client)

## License

Educational/Open Source

Enjoy playing Bomberman Multiplayer! 🎮💣

