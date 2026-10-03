package com.example.server;

import com.example.server.model.ServerBomb;
import com.example.server.model.ServerExplosion;
import com.example.server.model.ServerMapFactory;
import com.example.server.model.ServerPlayer;
import com.example.server.model.TileType;
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
    private int nextPlayerId = 1;

    // Authoritative game state
    private TileType[][] map;
    private final Map<Integer, ServerPlayer> players = Collections.synchronizedMap(new LinkedHashMap<>());
    private final List<ServerBomb> bombs = Collections.synchronizedList(new ArrayList<>());
    private final List<ServerExplosion> explosions = Collections.synchronizedList(new ArrayList<>());

    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private volatile boolean running = true;
    private final Gson gson = new Gson();

    public GameServer() {
        this.map = ServerMapFactory.createDefaultMap();
    }

    public static void main(String[] args) {
        SpringApplication.run(ServerApplication.class, args);
    }

    @Override
    public void run(String... args) {
        System.out.println("[Server] Bomberman Game Server initialized with WebSocket endpoint: ws://localhost:" + port + "/ws/game");
        System.out.println("[Server] Waiting for players to connect (2-4 players required)...");

        // Start physics / game update loop
        executorService.submit(this::gameLoop);
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

        // Update explosions
        Iterator<ServerExplosion> it = explosions.iterator();
        while (it.hasNext()) {
            ServerExplosion ex = it.next();
            ex.tick(deltaMs);
            if (ex.isExpired()) {
                it.remove();
            }
        }
    }

    private void detonate(ServerBomb b) {
        ServerExplosion ex = new ServerExplosion();
        int bx = b.tileX;
        int by = b.tileY;

        // center
        ex.addTile(bx, by);
        checkPlayersKill(bx, by);

        // four directions
        int[][] dirs = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        for (int[] d : dirs) {
            for (int step = 1; step <= b.radius; step++) {
                int nx = bx + d[0] * step;
                int ny = by + d[1] * step;
                if (nx < 0 || ny < 0 || ny >= map.length || nx >= map[0].length) break;
                if (map[ny][nx] == TileType.HARD_WALL) break;
                ex.addTile(nx, ny);
                // destroy soft block
                if (map[ny][nx] == TileType.SOFT_BLOCK) {
                    map[ny][nx] = TileType.FLOOR;
                    break;
                }
                // check players
                checkPlayersKill(nx, ny);
            }
        }
        explosions.add(ex);
    }

    private void checkPlayersKill(int tileX, int tileY) {
        for (ServerPlayer p : players.values()) {
            if (p.alive) {
                int playerTileX = p.x / GameConstants.TILE_SIZE;
                int playerTileY = p.y / GameConstants.TILE_SIZE;
                if (playerTileX == tileX && playerTileY == tileY) {
                    p.alive = false;
                    System.out.println("[Server] Player " + p.name + " eliminated!");
                }
            }
        }
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

        // Convert map
        for (int y = 0; y < map.length; y++) {
            for (int x = 0; x < map[0].length; x++) {
                data.map[y][x] = map[y][x].ordinal();
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

        bombs.add(new ServerBomb(tileX, tileY, 2000, 2));
    }

    private synchronized void resetGame() {
        map = ServerMapFactory.createDefaultMap();
        bombs.clear();
        explosions.clear();
        for (ServerPlayer p : players.values()) {
            p.reset();
        }
        System.out.println("[Server] Game reset!");
    }

    private synchronized int allocatePlayerId() {
        if (nextPlayerId > GameConstants.MAX_PLAYERS) {
            return -1; // Game full
        }
        return nextPlayerId++;
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
