# Architecture Overview

## System Architecture

```
┌────────────────────────────────────────────────────────────────┐
│                      NETWORK LAYER (TCP/JSON)                  │
└────────────────────────────────────────────────────────────────┘
            ▲                    ▲                    ▲
            │                    │                    │
        Port 9876            Port 9876            Port 9876
            │                    │                    │
      ┌─────▼──────────┐  ┌──────▼──────┐  ┌────────▼───────┐
      │  Client #1     │  │  Client #2   │  │   Client #3    │
      │  (Swing GUI)   │  │  (Swing GUI) │  │  (Swing GUI)   │
      │  ┌──────────┐  │  │ ┌──────────┐ │  │ ┌───────────┐  │
      │  │MultiplayerApp│  │ │MultiplayerApp  │  │MultiplayerApp│  │
      │  └────┬─────┘  │  │ └────┬────┘ │  │ └────┬──────┘  │
      │       │        │  │      │      │  │      │         │
      │  ┌────▼───────────┼──────▼──────┼──┼──────▼─────┐   │
      │  │ NetworkClient   │             │  │             │   │
      │  │ (TCP Socket)    │             │  │             │   │
      │  └─────┬──────────┘             │  │             │   │
      └────────┼────────────────────────┘  │             │   │
               │                           │             │   │
               └─────────────────────────────────────────┘   │
                              ▼                                │
        ┌─────────────────────────────────────────────────────┐
        │            GAME SERVER (CLI)                        │
        │  Port: 9876 (configurable)                          │
        │                                                      │
        │  ┌────────────────────────────────────────────┐    │
        │  │ GameServer.main()                          │    │
        │  │ - Accepts 4 client connections             │    │
        │  │ - Manages game loop (100ms ticks)          │    │
        │  │ - Broadcasts state (10 FPS)                │    │
        │  └────────────────┬───────────────────────────┘    │
        │                   │                                 │
        │  ┌────────────────▼───────────────────────────┐    │
        │  │ Game State                                 │    │
        │  │ - TileType[][] map                         │    │
        │  │ - Map<Integer, ServerPlayer> players       │    │
        │  │ - List<ServerBomb> bombs                   │    │
        │  │ - List<ServerExplosion> explosions         │    │
        │  └────────────────┬───────────────────────────┘    │
        │                   │                                 │
        │  ┌────────────────▼───────────────────────────┐    │
        │  │ Game Logic                                 │    │
        │  │ - updateGameState()                        │    │
        │  │ - detonate()                               │    │
        │  │ - checkPlayersKill()                       │    │
        │  │ - createGameStateData()                    │    │
        │  └────────────────┬───────────────────────────┘    │
        │                   │                                 │
        │  ┌────────────────▼───────────────────────────┐    │
        │  │ Client Handlers (Thread Pool)              │    │
        │  │ - ClientHandler #1                         │    │
        │  │ - ClientHandler #2                         │    │
        │  │ - ClientHandler #3                         │    │
        │  │ - ClientHandler #4                         │    │
        │  │                                            │    │
        │  │ Each handles:                              │    │
        │  │ - Player join                              │    │
        │  │ - Player input                             │    │
        │  │ - Message sending                          │    │
        │  └────────────────────────────────────────────┘    │
        └─────────────────────────────────────────────────────┘
```

## Message Flow Diagram

```
PLAYER JOINING:
┌────────┐                                        ┌────────┐
│ Client │                                        │ Server │
└────┬───┘                                        └───┬────┘
     │                                                │
     │ 1. Connect to localhost:9876                  │
     ├───────────────────────────────────────────────>
     │                                                │
     │ 2. PLAYER_JOIN message                        │
     │    {playerName: "Alice"}                      │
     ├───────────────────────────────────────────────>
     │                                                │ 3. Create ServerPlayer
     │                                                │ 4. Add to players map
     │                                                │ 5. Add to connectedClients
     │                                                │
     │ 6. SERVER_RESPONSE message                    │
     │    {success: true, playerId: 1}               │
     <───────────────────────────────────────────────┤
     │                                                │
     └ Show Game Window

GAMEPLAY LOOP:
┌────────┐                                        ┌────────┐
│ Client │                                        │ Server │
└────┬───┘                                        └───┬────┘
     │                                                │
     │ Keyboard: W pressed                           │
     │                                                │
     │ PLAYER_INPUT: {playerId: 1, action: "UP"}    │
     ├───────────────────────────────────────────────>
     │                                                │ Apply movement
     │                                                │
     │<─────────── GAME_STATE (10 FPS) ─────────────<┤
     │ {players, bombs, explosions, map}             │
     │                                                │
     │ Render all objects                            │
     │                                                │
     └─ Display updated game state

PLAYER LEAVING:
┌────────┐                                        ┌────────┐
│ Client │                                        │ Server │
└────┬───┘                                        └───┬────┘
     │                                                │
     │ Close connection                              │
     ├─────X─────────────────────────────────────────>
     │                                                │ Detect close
     │                                                │ Remove player
     │                                                │ Cleanup
```

## Class Hierarchy

```
Package: com.example
├── GameObject (abstract)
│   ├── Character (abstract)
│   │   └── Player (local player)
│   └── (other game objects)
├── Bomb (bomb mechanics)
├── Explosion (explosion mechanics)
├── TileType (enum)
├── DemoMapFactory (map generation)
├── MapPanel (JPanel - rendering)
├── GameController (input handling)
├── App (single-player main)
└── MultiplayerApp (multiplayer main)

Package: com.example.server
└── GameServer
    ├── ServerPlayer (inner class)
    ├── ServerBomb (inner class)
    ├── ServerExplosion (inner class)
    └── ClientHandler (inner class, Runnable)

Package: com.example.client
└── NetworkClient
    ├── Connection management
    ├── Message sending
    └── Callback-based updates

Package: com.example.network
├── GameMessage (abstract)
│   ├── PlayerJoinMessage
│   ├── PlayerInputMessage
│   ├── GameStateMessage
│   └── ServerResponseMessage
└── GameStateData (nested classes for serialization)
    ├── PlayerData
    ├── BombData
    └── ExplosionData
```

## Data Flow Diagram

```
INPUT:
┌─────────────────┐
│ User Keyboard   │
└────────┬────────┘
         │ WASD/SPACE/R
         ▼
┌──────────────────────┐
│ GameController       │
│ (KeyListener)        │
└────────┬─────────────┘
         │ Map key to action
         ▼
┌──────────────────────┐
│ NetworkClient.       │
│ sendPlayerInput()    │
└────────┬─────────────┘
         │ PlayerInputMessage (JSON)
         ▼
    [NETWORK]
         │ TCP Socket
         ▼
┌──────────────────────┐
│ GameServer           │
│ handlePlayerInput()  │
└────────┬─────────────┘
         │ Apply to ServerPlayer
         ▼
┌──────────────────────┐
│ GameState            │
│ (updated)            │
└──────────────────────┘

OUTPUT:
┌──────────────────────┐
│ GameServer.          │
│ gameLoop() (10 FPS)  │
└────────┬─────────────┘
         │ Update bombs, explosions
         ▼
┌──────────────────────┐
│ createGameStateData()│
└────────┬─────────────┘
         │ Convert to JSON
         ▼
┌──────────────────────┐
│ GameStateMessage     │
└────────┬─────────────┘
         │ Send to all ClientHandlers
         ▼
    [NETWORK]
         │ TCP Socket broadcast
         ▼
┌──────────────────────┐
│ NetworkClient.       │
│ listenForMessages()  │
└────────┬─────────────┘
         │ Parse JSON
         ▼
┌──────────────────────┐
│ GameMessage callback │
│ onGameStateUpdate()  │
└────────┬─────────────┘
         │ Update local player, remote players
         ▼
┌──────────────────────┐
│ MapPanel.            │
│ updateGameState()    │
└────────┬─────────────┘
         │ Store bombs, explosions
         ▼
┌──────────────────────┐
│ MapPanel.            │
│ paintComponent()     │
└────────┬─────────────┘
         │ Render at 60 FPS
         ▼
┌──────────────────────┐
│ Screen Display       │
└──────────────────────┘
```

## Thread Model

```
Main Thread:
│
├─ Server Socket Listener
│  └─ Accepts incoming connections
│
├─ Game Loop Thread (Executor)
│  └─ updateGameState() every 100ms
│
└─ Client Handler Threads (Executor - CachedThreadPool)
   ├─ ClientHandler #1
   │  └─ listenForMessages() (blocking read)
   │
   ├─ ClientHandler #2
   │  └─ listenForMessages() (blocking read)
   │
   ├─ ClientHandler #3
   │  └─ listenForMessages() (blocking read)
   │
   └─ ClientHandler #4
      └─ listenForMessages() (blocking read)

STATE SYNCHRONIZATION:
├─ ConcurrentHashMap<Integer, ClientHandler> (thread-safe)
├─ ConcurrentHashMap<Integer, ServerPlayer> (thread-safe)
├─ ConcurrentList<ServerBomb> (thread-safe)
└─ ConcurrentList<ServerExplosion> (thread-safe)
```

## Event Sequence

```
GAME START SEQUENCE:
1. Start: ./run-server.sh
2. ServerSocket listening on 0.0.0.0:9876
3. ExecutorService.submit(gameLoop) - starts 100ms ticker
4. ExecutorService starts (accepts incoming connections)

CLIENT JOIN SEQUENCE:
1. Start: ./run-client.sh localhost 9876
2. Show connection dialog (MultiplayerApp.showConnectionDialog)
3. User enters name, clicks "Connect"
4. NetworkClient.connect() - opens TCP socket
5. NetworkClient.joinGame(name) - sends PLAYER_JOIN
6. Server: ClientHandler receives PLAYER_JOIN
7. Server: allocatePlayerId() returns playerId 1
8. Server: ServerPlayer created and stored
9. Server: sendMessage() returns SERVER_RESPONSE
10. Client: onServerResponse() called with success=true
11. Client: Connection dialog closes
12. Client: Game window (MapPanel) opens

GAMEPLAY SEQUENCE (EVERY 100ms):
1. Server: gameLoop() tick
2. Server: updateGameState()
   - Update bomb fuses
   - Check bomb detonation
   - Update explosions
3. Server: broadcastGameState()
   - createGameStateData() (convert to JSON)
   - sendMessage() to all connected clients
4. Client: GameStateMessage received
5. Client: onGameStateUpdate() called
   - Update player positions
   - Update map
   - Update bombs, explosions
   - Call mapPanel.updateGameState()
6. Client: MapPanel.repaint()
   - paintComponent() called at 60 FPS (Swing timer)
   - All objects rendered

BOMB PLACEMENT SEQUENCE:
1. Client: User presses SPACE
2. GameController.KeyListener: keyPressed() event
3. GameController: sendPlayerInput("BOMB")
4. NetworkClient: sendMessage(PlayerInputMessage)
5. Server: ClientHandler receives message
6. Server: handlePlayerInput() with action="BOMB"
7. Server: placeBomb(player) creates ServerBomb
8. Next gameLoop tick: bomb added to state
9. gameLoop: 10 ticks later (1000ms = fuse time)
10. gameLoop: detonate(bomb)
11. gameLoop: createExplosion(), checkPlayersKill()
12. broadcastGameState: bomb gone, explosion appears
13. Client: Renders explosion, players update
```

## Performance Profile

```
SERVER SIDE:
┌─────────────────────────────────────┐
│ Game Loop (100ms)                   │
├─────────────────────────────────────┤
│ updateGameState()          5ms       │
│ broadcastGameState()       10ms      │
│ JSON serialization         5ms       │
│ Network send (4 clients)   10ms      │
│ TOTAL                      30ms      │
│ IDLE                       70ms      │
└─────────────────────────────────────┘

CLIENT SIDE:
┌─────────────────────────────────────┐
│ Receive Game State         5ms       │
│ JSON parsing               3ms       │
│ Update UI                  2ms       │
│ Render (60 FPS)            ~17ms     │
└─────────────────────────────────────┘

NETWORK:
- LAN latency: 50-100ms
- Bandwidth: ~2KB per state × 10/sec = 20 KB/s
- Message size: Varies 1-3KB based on game state
```

## Scalability

```
Current Design:
- Players per game: 4 (hardcoded MAX_PLAYERS)
- Games per server: 1 (single game instance)
- Connections per server: 4 concurrent
- Network: TCP socket per client

Potential Improvements:
- Multiple game rooms: 1 server, N games (rooms)
- Horizontal scaling: Multiple servers with load balancing
- WebSocket support: Browser-based clients
- Database: Persistent game state and player stats
```

This completes the architecture documentation. See README.md and MULTIPLAYER_GUIDE.md for usage instructions.

