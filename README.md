# 2012 Bomberman - Multiplayer Edition

A Java-based multiplayer Bomberman game with a client-server architecture supporting 2-4 players.

## Features

- **Multiplayer Support**: Play with 2-4 players in real-time
- **Client-Server Architecture**: Dedicated server that manages game state, clients connect to play
- **Network Synchronization**: Real-time game state updates to all connected players
- **Player Positions**: See all players' positions in real-time
- **Bombs & Explosions**: Place bombs that explode and destroy soft blocks
- **Collision Detection**: Walk-through walls and bomb blast mechanics
- **Respawn**: Press R to reset the game

## Requirements

- Java 17 or higher
- Maven 3.6+ (for building)
- macOS or Linux (Windows support via WSL)

## Quick Start

### 1. Install Dependencies

Make sure Maven is installed:
```bash
brew install maven  # macOS
# or use your system's package manager
```

### 2. Start the Server

```bash
chmod +x run-server.sh
./run-server.sh 9876
```

The server will start on port 9876 (customizable).

Output:
```
🎮 Bomberman Server started on port 9876
⏳ Waiting for players to connect (2-4 players required)...
```

### 3. Launch Client(s)

In a new terminal, run:
```bash
chmod +x run-client.sh
./run-client.sh localhost 9876
```

This will open a connection dialog. Enter your player name and click "Connect".

Repeat for each player (2-4 players total).

### 4. Play!

Once at least 2 players are connected, the game will start automatically.

**Controls:**
- **W**: Move up
- **S**: Move down
- **A**: Move left
- **D**: Move right
- **SPACE**: Place bomb
- **R**: Reset game

## Advanced Usage

### Custom Port

```bash
# Start server on custom port
./run-server.sh 5555

# Connect to custom port
./run-client.sh localhost 5555
```

### Remote Connection

To connect to a server on a different machine:
```bash
./run-client.sh <server-ip> 9876
```

### Manual Build and Run

```bash
# Build
mvn clean package

# Run server
java -cp "target/classes:$(mvn dependency:build-classpath -q -Dmdep.outputFile=/dev/stdout)" \
  com.example.server.GameServer 9876

# Run client
java -cp "target/classes:$(mvn dependency:build-classpath -q -Dmdep.outputFile=/dev/stdout)" \
  com.example.MultiplayerApp localhost 9876
```

## Game Mechanics

### Map
- 13x11 tile grid
- Hard walls (indestructible)
- Soft blocks (destructible)
- Floor tiles (walkable)

### Players
- 4 spawn corners (top-left, top-right, bottom-left, bottom-right)
- Each player gets a unique color
- Players are eliminated when hit by an explosion
- Reset the game with R key

### Bombs
- Placed with SPACE
- Explode after ~2 seconds
- Blast radius of 2 tiles in each direction
- Destroy soft blocks

### Explosions
- Visible for ~700ms
- Kill any player in the blast area
- Stop at hard walls and destroyed soft blocks

## Architecture

### Server (`GameServer.java`)
- Accepts up to 4 client connections
- Manages authoritative game state
- Broadcasts state updates 10 times per second
- Handles player input messages
- Runs bomb/explosion physics

### Client (`MultiplayerApp.java`, `NetworkClient.java`)
- Connects to server
- Sends player input (WASD, SPACE, R)
- Receives game state updates
- Renders all players and game objects
- Updates local display at 60 FPS (configurable)

### Network Protocol
Messages are JSON-serialized and sent over TCP:

**Player Join:**
```json
{
  "messageType": "PLAYER_JOIN",
  "playerName": "Alice"
}
```

**Player Input:**
```json
{
  "messageType": "PLAYER_INPUT",
  "playerId": 1,
  "action": "UP"
}
```

**Game State:**
```json
{
  "messageType": "GAME_STATE",
  "gameState": {
    "map": [...],
    "players": [...],
    "bombs": [...],
    "explosions": [...]
  }
}
```

## Troubleshooting

### "Connection refused" error
- Make sure the server is running on the specified port
- Check firewall settings
- Verify correct hostname/IP

### "Game is full" message
- Maximum 4 players per game
- Wait for a game to finish or start a new server on a different port

### Game freezes or lags
- Check network connection
- Reduce firewall restrictions
- Close other applications using network

### Maven not found
```bash
brew install maven
export PATH="/usr/local/bin:$PATH"  # Add to PATH if needed
```

## Project Structure

```
src/main/java/com/example/
├── App.java                      # Original single-player app
├── MultiplayerApp.java           # Multiplayer client launcher
├── GameController.java           # Input handling (network version)
├── MapPanel.java                 # Game rendering (multiplayer)
├── Player.java                   # Local player
├── Character.java                # Character base class
├── GameObject.java               # Game object base
├── Bomb.java                     # Bomb mechanics
├── Explosion.java                # Explosion mechanics
├── TileType.java                 # Map tile types
├── DemoMapFactory.java           # Map generation
├── client/
│   └── NetworkClient.java        # Network communication (client-side)
├── server/
│   └── GameServer.java           # Game server (authoritative)
└── network/
    ├── GameMessage.java          # Message base class
    ├── PlayerJoinMessage.java    # Join message
    ├── PlayerInputMessage.java   # Input message
    ├── GameStateMessage.java     # State message
    ├── GameStateData.java        # Serializable state
    └── ServerResponseMessage.java # Response message
```

## Development

### Adding Features

1. **New Game Mechanics**: Modify `GameServer` physics
2. **New Messages**: Create new `GameMessage` subclass in `network/`
3. **UI Improvements**: Modify `MapPanel.java` rendering
4. **New Items/Power-ups**: Extend `GameStateData` and update serialization

### Testing

Run unit tests:
```bash
mvn test
```

## Known Limitations

- No persistent storage (game state lost on server restart)
- No spectator mode
- No chat system
- All players on same map only

## Future Enhancements

- [ ] Multiple game rooms/lobbies
- [ ] Power-ups (speed, blast radius)
- [ ] Player statistics tracking
- [ ] Replay system
- [ ] Mobile client support
- [ ] Spectator mode
- [ ] Custom map support
- [ ] Game recording

## License

Educational/Open Source

## Credits

Original 2012 Bomberman implementation adapted for multiplayer.
