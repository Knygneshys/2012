package com.example.server;

import com.example.server.model.ServerBomb;
import com.example.server.model.blocks.ServerBlock;
import com.example.server.model.ServerExplosion;
import com.example.server.model.ServerMapFactory;
import com.example.server.model.ServerNpc;
import com.example.server.model.ServerPlayer;
import com.example.server.model.ServerPowerup;
import com.example.server.network.GameStateData;
import com.example.server.network.GameStateMessage;
import com.example.server.network.PlayerInputMessage;
import com.example.server.network.PlayerJoinMessage;
import com.example.server.network.ServerResponseMessage;
import com.google.gson.Gson;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

/**
 * Multiplayer Bomberman Game Server (Spring Component).
 * Manages game state, physics, and WebSocket client connections.
 */
@Component
public class GameServer implements CommandLineRunner {

    @Value("${server.port:8080}")
    private int port = GameConstants.DEFAULT_PORT;

    private final Map<Integer, WebSocketSession> clientSessions = Collections.synchronizedMap(new LinkedHashMap<>());
private final Map<String, Integer> sessionIdToPlayerId = Collections.synchronizedMap(new HashMap<>());
    // Authoritative game state
    private ServerBlock[][] map;
    private final Map<Integer, ServerPlayer> players = Collections.synchronizedMap(new LinkedHashMap<>());
    private final List<ServerBomb> bombs = Collections.synchronizedList(new ArrayList<>());
    private final List<ServerExplosion> explosions = Collections.synchronizedList(new ArrayList<>());
    private final List<ServerNpc> npcs = Collections.synchronizedList(new ArrayList<>());
    private final List<ServerPowerup> powerups = Collections.synchronizedList(new ArrayList<>());

    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private volatile boolean running = true;
    private final Gson gson = new Gson();
    private final Random random = new Random();

    public GameServer() {
        this.map = ServerMapFactory.createDefaultMap();
        spawnNpcs();
    }

    public static void main(String[] args) {
        SpringApplication.run(ServerApplication.class, args);
    }

    @Override
    public void run(String... args) {
        System.out.println("[Server] Bomberman Game Server initialized with WebSocket endpoint: ws://localhost:" + port + "/ws/game");
        System.out.println("[Server] Waiting for players to connect (2-4 players required)...");

        // Start physics / game update loop. execute(), not submit(): submit()
        // captures a failure in a Future nobody reads, so any exception thrown
        // below would freeze the game for good without a word in the log.
        executorService.execute(this::gameLoop);
    }

    private void gameLoop() {
        long lastUpdateTime = System.currentTimeMillis();

        while (running) {
            try {
                long now = System.currentTimeMillis();
                long deltaMs = now - lastUpdateTime;

                if (deltaMs >= GameConstants.TICK_MS) {
                    updateGameState((int) deltaMs);
                    broadcastGameState();
                    lastUpdateTime = now;
                }

                Thread.sleep(GameConstants.TICK_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (RuntimeException e) {
                // One bad tick must not end the game for everyone.
                System.err.println("[Server] Error in game loop: " + e);
                e.printStackTrace();
                lastUpdateTime = System.currentTimeMillis();
            }
        }
    }

    private void updateGameState(int deltaMs) {
        // Update bombs
        List<ServerBomb> detonated = new ArrayList<>();
        for (ServerBomb b : bombs) {
            if (b.tick(deltaMs)) {
                detonated.add(b);
            }
        }
        for (ServerBomb b : detonated) {
            detonate(b);
            bombs.remove(b);
        }

        // Move NPCs before the fire is applied, so walking into a burning tile cannot
        // survive until the next tick
        for (ServerNpc npc : npcs) {
            npc.tick(deltaMs, map);
        }

        // Update explosions
        Iterator<ServerExplosion> it = explosions.iterator();
        while (it.hasNext()) {
            ServerExplosion ex = it.next();
            ex.tick(deltaMs);
            if (ex.isExpired()) {
                it.remove();
            }
        }

        // Burning tiles keep hurting whatever stands in them, so a character
        // that walks into a live explosion is caught by it too
        for (ServerExplosion ex : explosions) {
            for (int[] tile : ex.tiles) {
                applyBlast(tile[0], tile[1]);
            }
        }

        checkNpcContact();
        collectPowerups();
    }

    private void detonate(ServerBomb b) {
        ServerExplosion ex = new ServerExplosion();
        int bx = b.tileX;
        int by = b.tileY;

        // center
        ex.addTile(bx, by);
        applyBlast(bx, by);

        // four directions
        int[][] dirs = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        for (int[] d : dirs) {
            for (int step = 1; step <= b.radius; step++) {
                int nx = bx + d[0] * step;
                int ny = by + d[1] * step;
                if (nx < 0 || ny < 0 || ny >= map.length || nx >= map[0].length) break;

                ServerBlock block = map[ny][nx];
                if (block.isDestructible()) {
                    // A breakable wall is swallowed by the blast: it shows up in
                    // the fire, turns into a passage and may drop a powerup.
                    ex.addTile(nx, ny);
                    map[ny][nx] = block.destroyed();
                    maybeSpawnPowerup(nx, ny);
                    break;
                }
                if (block.stopsBlast()) {
                    // A wall survives, so the blast neither reaches nor passes it.
                    break;
                }

                ex.addTile(nx, ny);
                // check characters in the blast
                applyBlast(nx, ny);
            }
        }
        explosions.add(ex);
    }

    /**
     * Applies a blast to a single tile, eliminating whatever stands on it.
     */
    private void applyBlast(int tileX, int tileY) {
        for (ServerPlayer p : players.values()) {
            if (p.alive && p.overlapsTile(tileX, tileY)) {
                p.kill();
                System.out.println("[Server] Player " + p.name + " eliminated!");
            }
        }
        for (ServerNpc npc : npcs) {
            if (npc.alive && npc.overlapsTile(tileX, tileY)) {
                npc.kill();
                System.out.println("[Server] NPC " + npc.id + " eliminated!");
            }
        }
    }

    /**
     * Drops a powerup on the tile a breakable wall was just destroyed on.
     */
    private void maybeSpawnPowerup(int tileX, int tileY) {
        if (powerups.size() >= GameConstants.MAX_POWERUPS) return;
        if (random.nextDouble() >= GameConstants.POWERUP_DROP_CHANCE) return;

        ServerPowerup powerup = new ServerPowerup(ServerPowerup.Kind.random(random), tileX, tileY);
        powerups.add(powerup);
        System.out.println("[Server] Dropped " + powerup.kind + " at " + tileX + "," + tileY);
    }

    /**
     * Players walk onto a powerup tile to collect it.
     */
    private void collectPowerups() {
        Iterator<ServerPowerup> it = powerups.iterator();
        while (it.hasNext()) {
            ServerPowerup powerup = it.next();
            for (ServerPlayer p : players.values()) {
                if (p.alive && p.overlapsTile(powerup.tileX, powerup.tileY)) {
                    powerup.applyTo(p);
                    System.out.println("[Server] Player " + p.name + " collected " + powerup.kind);
                    it.remove();
                    break;
                }
            }
        }
    }

    /**
     * An NPC sharing a tile with a player takes it down with it.
     */
    private void checkNpcContact() {
        for (ServerNpc npc : npcs) {
            if (!npc.alive) continue;
            for (ServerPlayer p : players.values()) {
                if (p.alive && npc.overlaps(p)) {
                    p.kill();
                    System.out.println("[Server] Player " + p.name + " caught by NPC " + npc.id + "!");
                }
            }
        }
    }

    /**
     * Places a fresh NPC on a random free tile, away from the player spawns.
     */
    private void spawnNpcs() {
        npcs.clear();
        for (int i = 0; i < GameConstants.NPC_COUNT; i++) {
            int[] spot = findFreeTile();
            if (spot == null) break;

            ServerNpc npc = new ServerNpc(i + 1,
                    spot[0] * GameConstants.TILE_SIZE,
                    spot[1] * GameConstants.TILE_SIZE,
                    GameConstants.NPC_MOVE_SPEED,
                    GameConstants.NPC_COLORS[i % GameConstants.NPC_COLORS.length],
                    random);
            npcs.add(npc);
        }
    }

    private int[] findFreeTile() {
        for (int attempt = 0; attempt < GameConstants.NPC_SPAWN_ATTEMPTS; attempt++) {
            int x = 1 + random.nextInt(GameConstants.MAP_WIDTH - 2);
            int y = 1 + random.nextInt(GameConstants.MAP_HEIGHT - 2);
            if (!map[y][x].isPassable()) continue;
            if (tooCloseToPlayerSpawn(x, y)) continue;
            return new int[]{x, y};
        }
        return null;
    }

    /**
     * Keeps NPCs away from the tiles players spawn on, so nobody is caught
     * before the round has started.
     */
    private boolean tooCloseToPlayerSpawn(int x, int y) {
        for (int i = 0; i < GameConstants.MAX_PLAYERS; i++) {
            int dx = Math.abs(x - GameConstants.SPAWN_X[i]);
            int dy = Math.abs(y - GameConstants.SPAWN_Y[i]);
            if (dx <= GameConstants.NPC_SPAWN_CLEARANCE && dy <= GameConstants.NPC_SPAWN_CLEARANCE) {
                return true;
            }
        }
        return false;
    }

    private void broadcastGameState() {
        if (clientSessions.isEmpty()) {
            return;
        }

        GameStateData state = createGameStateData();
        GameStateMessage message = new GameStateMessage(state);
        String json = gson.toJson(message);
        TextMessage textMessage = new TextMessage(json);

        synchronized (clientSessions) {
            for (WebSocketSession session : clientSessions.values()) {
                if (session.isOpen()) {
                    try {
                        synchronized (session) {
                            session.sendMessage(textMessage);
                        }
                    } catch (IOException e) {
                        System.err.println("[Server] Error sending state to session " + session.getId() + ": " + e.getMessage());
                    }
                }
            }
        }
    }

    private GameStateData createGameStateData() {
        GameStateData data = new GameStateData();
        data.map = new int[map.length][map[0].length];

        // Convert map. The wire format is the TileType ordinal, so the blocks
        // carry their type rather than being described directly.
        for (int y = 0; y < map.length; y++) {
            for (int x = 0; x < map[0].length; x++) {
                data.map[y][x] = map[y][x].tileType().ordinal();
            }
        }

        // Convert players
        for (ServerPlayer p : players.values()) {
            data.players.add(new GameStateData.PlayerData(
                    p.id, p.name, p.x, p.y, p.alive, p.colorHex, p.moveSpeed
            ));
        }

        // Convert bombs
        for (ServerBomb b : bombs) {
            data.bombs.add(new GameStateData.BombData(b.tileX, b.tileY, b.remainingMs, b.radius));
        }

        // Convert explosions
        for (ServerExplosion ex : explosions) {
            int[][] tiles = new int[ex.tiles.size()][2];
            for (int i = 0; i < ex.tiles.size(); i++) {
                tiles[i] = ex.tiles.get(i);
            }
            data.explosions.add(new GameStateData.ExplosionData(tiles, ex.remainingMs));
        }

        // Convert NPCs
        for (ServerNpc npc : npcs) {
            data.npcs.add(new GameStateData.NpcData(npc.id, npc.x, npc.y, npc.moveSpeed, npc.alive, npc.colorHex));
        }

        // Convert powerups
        for (ServerPowerup powerup : powerups) {
            data.powerups.add(new GameStateData.PowerupData(
                    powerup.kind.ordinal(), powerup.tileX, powerup.tileY));
        }

        return data;
    }

    public synchronized void handlePlayerJoin(WebSocketSession session, PlayerJoinMessage msg) {
        int playerId = allocatePlayerId();

        if (playerId == -1) {
            ServerResponseMessage response = new ServerResponseMessage(false, "Game is full (max 4 players)", -1);
            sendMessage(session, gson.toJson(response));
            return;
        }

        clientSessions.put(playerId, session);
        sessionIdToPlayerId.put(session.getId(), playerId);

        ServerPlayer player = new ServerPlayer(
                playerId,
                msg.playerName,
                GameConstants.SPAWN_X[playerId - 1] * GameConstants.TILE_SIZE,
                GameConstants.SPAWN_Y[playerId - 1] * GameConstants.TILE_SIZE,
                GameConstants.PLAYER_COLORS[playerId - 1]
        );
        players.put(playerId, player);

        System.out.println("[Server] Player " + msg.playerName + " joined as Player #" + playerId);
        System.out.println("[Server] Connected players: " + clientSessions.size() + "/" + GameConstants.MAX_PLAYERS);

        ServerResponseMessage response = new ServerResponseMessage(
                true,
                "Welcome to Bomberman! You are Player #" + playerId,
                playerId
        );
        sendMessage(session, gson.toJson(response));
    }

    public void handlePlayerInput(WebSocketSession session, PlayerInputMessage msg) {
        Integer playerId = sessionIdToPlayerId.get(session.getId());
        if (playerId == null || !players.containsKey(playerId)) {
            return;
        }

        ServerPlayer player = players.get(playerId);
        switch (msg.action) {
            case "UP" -> player.move(0, -player.moveSpeed, map);
            case "DOWN" -> player.move(0, player.moveSpeed, map);
            case "LEFT" -> player.move(-player.moveSpeed, 0, map);
            case "RIGHT" -> player.move(player.moveSpeed, 0, map);
            case "BOMB" -> placeBomb(player);
            case "RESET" -> resetGame();
        }
    }

    public synchronized void handleSessionClosed(WebSocketSession session) {
        Integer playerId = sessionIdToPlayerId.remove(session.getId());
        if (playerId != null) {
            players.remove(playerId);
            clientSessions.remove(playerId);
            System.out.println("[Server] Player #" + playerId + " disconnected. Connected: " + clientSessions.size());
        }
    }

    private void sendMessage(WebSocketSession session, String json) {
        if (session != null && session.isOpen()) {
            try {
                synchronized (session) {
                    session.sendMessage(new TextMessage(json));
                }
            } catch (IOException e) {
                System.err.println("[Server] Error sending message: " + e.getMessage());
            }
        }
    }

    private void placeBomb(ServerPlayer p) {
        if (!p.alive) return;
        int px = p.x;
        int py = p.y;
        int tileX = (px + GameConstants.TILE_SIZE / 2) / GameConstants.TILE_SIZE;
        int tileY = (py + GameConstants.TILE_SIZE / 2) / GameConstants.TILE_SIZE;

        // Check if bomb already exists on that tile
        for (ServerBomb b : bombs) {
            if (b.tileX == tileX && b.tileY == tileY) return;
        }

        // Respect the bomb limit the player currently has
        if (countBombsOf(p) >= p.maxBombs) return;

        bombs.add(new ServerBomb(tileX, tileY, GameConstants.BOMB_FUSE_MS, p.bombRadius, p.id));
    }

    private int countBombsOf(ServerPlayer p) {
        int count = 0;
        for (ServerBomb b : bombs) {
            if (b.ownerId == p.id) {
                count++;
            }
        }
        return count;
    }

    private synchronized void resetGame() {
        map = ServerMapFactory.createDefaultMap();
        bombs.clear();
        explosions.clear();
        powerups.clear();
        spawnNpcs();
        for (ServerPlayer p : players.values()) {
            p.reset();
        }
        System.out.println("[Server] Game reset!");
    }

    private synchronized int allocatePlayerId() {
        // Hand out the lowest free slot. A monotonic counter would lock the
        // game out for good once MAX_PLAYERS joins had happened, even with
        // nobody connected, and ids must stay within the spawn table anyway.
        for (int id = 1; id <= GameConstants.MAX_PLAYERS; id++) {
            if (!players.containsKey(id)) {
                return id;
            }
        }
        return -1; // Game full
    }

    public boolean isRunning() {
        return running;
    }

    public int getPort() {
        return port;
    }

    public int getConnectedPlayerCount() {
        return clientSessions.size();
    }

    public int getMaxPlayers() {
        return GameConstants.MAX_PLAYERS;
    }

    public List<Map<String, Object>> getPlayerSummaryList() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (ServerPlayer p : players.values()) {
            Map<String, Object> info = new LinkedHashMap<>();
            info.put("id", p.id);
            info.put("name", p.name);
            info.put("x", p.x);
            info.put("y", p.y);
            info.put("alive", p.alive);
            info.put("color", p.colorHex);
            list.add(info);
        }
        return list;
    }

    @PreDestroy
    public void shutdown() {
        running = false;
        synchronized (clientSessions) {
            for (WebSocketSession session : clientSessions.values()) {
                if (session.isOpen()) {
                    try {
                        session.close();
                    } catch (IOException ignored) {
                    }
                }
            }
            clientSessions.clear();
            sessionIdToPlayerId.clear();
        }
        executorService.shutdown();
        try {
            executorService.awaitTermination(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
