# 2012 Bomberman - Multiplayer Edition

A Java-based multiplayer Bomberman game with a fully separated client-server architecture supporting 2-4 players.

```
mano-projektas/
├── client/          (Swing client code)
└── server/          (Spring Boot server code)
```

The client and server are completely decoupled: they do **not** share classes or classpath dependencies and communicate exclusively over the network via TCP sockets using JSON messages (plus Spring REST endpoints).

## Features

- **Multiplayer Support**: Play with 2-4 players in real-time
- **Separated Architecture**: Dedicated Spring Boot server managing game state, independent Swing client
- **Network Communication**: Real-time game state synchronization over TCP sockets using JSON
- **Spring Boot REST API**: Query server health and active player list via HTTP endpoints (`/api/status`, `/api/players`)
- **Player Positions**: Real-time position updates across all connected clients
- **Bombs & Explosions**: Server-authoritative bomb detonation and soft block destruction mechanics
- **NPCs**: Server-simulated wandering characters that eliminate players they run into and die in blasts
- **Powerups**: Breakable walls sometimes drop a bonus (extra bomb, bigger blast, more speed) that players collect by walking over it
- **Collision Detection**: Server-authoritative wall and obstacle collision checks
- **Respawn**: Press R to reset the game

## Requirements

- Java 17 or higher
- Maven 3.6+

## Quick Start

### 1. Build the Entire Project

From the project root directory:
```bash
mvn clean compile
# or package executable JARs
mvn clean package -DskipTests
```

### 2. Start the Server (Spring Boot)

#### On Linux / macOS:
```bash
chmod +x run-server.sh
./run-server.sh 8080
```

#### On Windows:
```cmd
run-server.bat 8080
```

#### Or using Maven directly:
```bash
cd server
mvn spring-boot:run -Dspring-boot.run.arguments="8080"
```

The Spring Boot server will start:
- **TCP Game Socket**: `localhost:8080` (handles real-time game traffic)
- **HTTP REST API**: `http://localhost:8080/api/status` (status and player count)

### 3. Launch Client(s) (Java Swing)

In a new terminal window for each player (2-4 players):

#### On Linux / macOS:
```bash
chmod +x run-client.sh
./run-client.sh localhost 8080
```

#### On Windows:
```cmd
run-client.bat localhost 8080
```

#### Or using Maven directly:
```bash
cd client
mvn exec:java -Dexec.args="localhost 8080"
```

Enter your player name in the connection dialog and click **Connect**.

### 4. Controls

- **W**: Move up
- **S**: Move down
- **A**: Move left
- **D**: Move right
- **SPACE**: Place bomb
- **R**: Reset game

---

## Architecture & Decoupling

The repository is organized following the multi-module principle:

```
2012/
├── pom.xml                                   # Root aggregator POM
├── run-server.sh / run-server.bat           # Server launch scripts
├── run-client.sh / run-client.bat           # Client launch scripts
├── client/                                  # Swing Client Module
│   ├── pom.xml
│   └── src/main/java/com/example/client/
│       ├── MultiplayerApp.java               # Client GUI entry point
│       ├── App.java                          # Client demo launcher
│       ├── MapPanel.java                     # Swing JPanel game renderer
│       ├── GameController.java               # Swing input listener & state updater
│       ├── GameObject.java                   # Base game object (position + size)
│       ├── Character.java                    # Base character (movement, colour, alive)
│       ├── Player.java                       # Local client player model
│       ├── NPC.java                          # Server simulated non player character
│       ├── Bomb.java                         # Client bomb representation
│       ├── Powerup.java                      # Collectable bonus item
│       ├── Block.java                        # Base map tile
│       ├── Wall.java                         # Indestructible tile
│       ├── BreakableWall.java                # Destructible tile
│       ├── Passage.java                      # Walkable tile
│       ├── Explosion.java                    # Client explosion representation
│       ├── TileType.java                     # Client tile type enum
│       ├── DemoMapFactory.java               # Client map dimensions & preview
│       └── network/
│           ├── NetworkClient.java            # WebSocket client connection
│           ├── GameMessage.java              # Client network message base
│           ├── PlayerJoinMessage.java        # Join request DTO
│           ├── PlayerInputMessage.java       # Input event DTO
│           ├── GameStateMessage.java         # State broadcast DTO
│           ├── GameStateData.java            # Serializable state DTO
│           └── ServerResponseMessage.java     # Server response DTO
└── server/                                  # Spring Boot Server Module
    ├── pom.xml
    └── src/main/
        ├── java/com/example/server/
        │   ├── ServerApplication.java       # Spring Boot main application class
        │   ├── GameServer.java              # Authoritative game server (@Component, CommandLineRunner)
        │   ├── GameConstants.java           # Server-side constants (tiles, dimensions, ports, NPCs, powerups)
        │   ├── controller/
        │   │   └── ServerStatusController.java # Spring REST API (/api/status, /api/players)
        │   ├── model/
        │   │   ├── TileType.java            # Server tile type enum
        │   │   ├── ServerPlayer.java        # Server player entity & physics
        │   │   ├── ServerBomb.java          # Server bomb timer & detonation
        │   │   ├── ServerExplosion.java     # Server explosion propagator
        │   │   ├── ServerNpc.java           # Server NPC entity & wandering AI
        │   │   ├── ServerPowerup.java       # Server powerup drop & bonuses
        │   │   └── ServerMapFactory.java    # Authoritative map generator
        │   └── network/
        │       ├── GameWebSocketHandler.java # WebSocket endpoint (/ws/game) dispatcher
        │       ├── GameMessage.java          # Server network message base
        │       ├── PlayerJoinMessage.java    # Join request DTO
        │       ├── PlayerInputMessage.java   # Input event DTO
        │       ├── GameStateMessage.java     # State broadcast DTO
        │       ├── GameStateData.java        # Serializable state DTO
        │       └── ServerResponseMessage.java # Server response DTO
        └── resources/
            └── application.properties       # Server configuration (HTTP & TCP ports)
```

### No Shared Classes

- `client` and `server` have separate `pom.xml` configurations.
- Neither module imports or depends on classes from the other module.
- All coordination is performed strictly over the network using TCP sockets and JSON payloads.

### Tile Factory Method

Server map generation uses `ServerBlockFactory.placeBlock(...)`, which calls
the overridable `createBlock(...)` factory method. `WallFactory`,
`BreakableWallFactory`, and `PassageFactory` each create their corresponding
`ServerBlock` subclass. `ServerMapFactory` selects the creator by `TileType`;
the tile layout and network format stay the same.

Run the dependency-free factory checks from the project root:

```bash
mvn test-compile
java -cp server/target/classes:server/target/test-classes com.example.server.model.ServerBlockFactoryCheck
```

On Windows, use `;` instead of `:` between the two classpath directories.
This executable check must be run explicitly; it is not a JUnit test.
