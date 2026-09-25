package com.example.server;

import com.example.*;
import com.example.network.*;
import com.google.gson.Gson;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.*;
import java.awt.Color;

/**
 * Multiplayer Bomberman Server.
 * Manages up to 4 players in a single game session.
 */
public class GameServer {
    private static final int DEFAULT_PORT = 9876;
    private static final int MAX_PLAYERS = 4;
    private static final int TICK_MS = 10;
    private static final int[] SPAWN_X = {1, 11, 1, 11};
    private static final int[] SPAWN_Y = {1, 1, 9, 9};
    private static final Color[] PLAYER_COLORS = {Color.BLUE, Color.RED, Color.YELLOW, Color.GREEN};

    private ServerSocket serverSocket;
    private int port;
    private Map<Integer, ClientHandler> connectedClients = Collections.synchronizedMap(new LinkedHashMap<>());
    private int nextPlayerId = 1;

    // Game state
    private TileType[][] map;
    private Map<Integer, ServerPlayer> players = Collections.synchronizedMap(new LinkedHashMap<>());
    private List<ServerBomb> bombs = Collections.synchronizedList(new ArrayList<>());
    private List<ServerExplosion> explosions = Collections.synchronizedList(new ArrayList<>());

    private ExecutorService executorService;
    private volatile boolean running = true;
    private Gson gson = new Gson();

    public GameServer(int port) {
        this.port = port;
        this.executorService = Executors.newCachedThreadPool();
        this.map = DemoMapFactory.createDefaultMap();
    }

    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port: " + args[0]);
                System.exit(1);
            }
        }

        GameServer server = new GameServer(port);
        server.start();
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("🎮 Bomberman Server started on port " + port);
            System.out.println("⏳ Waiting for players to connect (2-4 players required)...");

            // Start game update thread
            executorService.submit(this::gameLoop);

            // Accept client connections
            while (running) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("🔌 New connection from: " + clientSocket.getInetAddress());
                executorService.submit(new ClientHandler(clientSocket));
            }
        } catch (IOException e) {
            if (running) {
                System.err.println("❌ Server error: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void gameLoop() {
        long lastUpdateTime = System.currentTimeMillis();

        while (running) {
            try {
                long now = System.currentTimeMillis();
                long deltaMs = now - lastUpdateTime;

                if (deltaMs >= TICK_MS) {
                    updateGameState((int) deltaMs);
                    broadcastGameState();
                    lastUpdateTime = now;
                }

                Thread.sleep(10);
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
        int[][] dirs = {{1,0},{-1,0},{0,1},{0,-1}};
        for (int[] d : dirs) {
            for (int step = 1; step <= b.radius; step++) {
                int nx = bx + d[0]*step;
                int ny = by + d[1]*step;
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
                int playerTileX = p.x / MapPanel.TILE_SIZE;
                int playerTileY = p.y / MapPanel.TILE_SIZE;
                if (playerTileX == tileX && playerTileY == tileY) {
                    p.alive = false;
                    System.out.println("💀 Player " + p.name + " killed!");
                }
            }
        }
    }

    private void broadcastGameState() {
        if (connectedClients.isEmpty()) {
            return;
        }

        GameStateData state = createGameStateData();
        GameStateMessage message = new GameStateMessage(state);
        String json = gson.toJson(message);

        for (ClientHandler client : connectedClients.values()) {
            client.sendMessage(json);
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
            String colorHex = String.format("#%02x%02x%02x",
                    p.color.getRed(), p.color.getGreen(), p.color.getBlue());
            data.players.add(new GameStateData.PlayerData(
                    p.id, p.name, p.x, p.y, p.alive, colorHex, p.moveSpeed
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
                int[] tile = ex.tiles.get(i);
                tiles[i] = tile;
            }
            data.explosions.add(new GameStateData.ExplosionData(tiles, ex.remainingMs));
        }

        return data;
    }

    private synchronized int allocatePlayerId() {
        if (nextPlayerId > MAX_PLAYERS) {
            return -1; // Game full
        }
        return nextPlayerId++;
    }

    public void shutdown() {
        running = false;
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
            executorService.shutdown();
            executorService.awaitTermination(5, TimeUnit.SECONDS);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Inner class to handle each client connection
    private class ClientHandler implements Runnable {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private int playerId = -1;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                String line;
                while ((line = in.readLine()) != null && running) {
                    handleMessage(line);
                }
            } catch (IOException e) {
                if (running) {
                    System.err.println("❌ Client handler error: " + e.getMessage());
                }
            } finally {
                cleanup();
            }
        }

        private void handleMessage(String jsonMessage) {
            try {
                GameMessage msg = parseMessage(jsonMessage);

                if (msg instanceof PlayerJoinMessage) {
                    handlePlayerJoin((PlayerJoinMessage) msg);
                } else if (msg instanceof PlayerInputMessage) {
                    handlePlayerInput((PlayerInputMessage) msg);
                }
            } catch (Exception e) {
                System.err.println("❌ Error handling message: " + e.getMessage());
            }
        }

        private GameMessage parseMessage(String json) {
            // Parse the message type first
            if (json.contains("\"messageType\":\"PLAYER_JOIN\"")) {
                return gson.fromJson(json, PlayerJoinMessage.class);
            } else if (json.contains("\"messageType\":\"PLAYER_INPUT\"")) {
                return gson.fromJson(json, PlayerInputMessage.class);
            } else if (json.contains("\"messageType\":\"GAME_STATE\"")) {
                return gson.fromJson(json, GameStateMessage.class);
            }
            return null;
        }

        private void handlePlayerJoin(PlayerJoinMessage msg) {
            playerId = allocatePlayerId();

            if (playerId == -1) {
                ServerResponseMessage response = new ServerResponseMessage(false, "Game is full (max 4 players)", -1);
                sendMessage(gson.toJson(response));
                return;
            }

            // Create player
            ServerPlayer player = new ServerPlayer(playerId, msg.playerName,
                    SPAWN_X[playerId - 1] * MapPanel.TILE_SIZE,
                    SPAWN_Y[playerId - 1] * MapPanel.TILE_SIZE,
                    PLAYER_COLORS[playerId - 1]);
            players.put(playerId, player);
            connectedClients.put(playerId, this);

            System.out.println("✅ Player " + msg.playerName + " joined as Player #" + playerId);
            System.out.println("📊 Connected players: " + connectedClients.size() + "/" + MAX_PLAYERS);

            ServerResponseMessage response = new ServerResponseMessage(true,
                    "Welcome to Bomberman! You are Player #" + playerId, playerId);
            sendMessage(gson.toJson(response));
        }

        private void handlePlayerInput(PlayerInputMessage msg) {
            if (playerId == -1 || !players.containsKey(playerId)) {
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

        private void placeBomb(ServerPlayer p) {
            if (!p.alive) return;
            int px = p.x;
            int py = p.y;
            int tileX = (px + MapPanel.TILE_SIZE/2) / MapPanel.TILE_SIZE;
            int tileY = (py + MapPanel.TILE_SIZE/2) / MapPanel.TILE_SIZE;

            // Check if bomb already exists on that tile
            for (ServerBomb b : bombs) {
                if (b.tileX == tileX && b.tileY == tileY) return;
            }

            bombs.add(new ServerBomb(tileX, tileY, 2000, 2));
        }

        private void resetGame() {
            map = DemoMapFactory.createDefaultMap();
            bombs.clear();
            explosions.clear();
            for (ServerPlayer p : players.values()) {
                p.reset();
            }
            System.out.println("🔄 Game reset!");
        }

        public void sendMessage(String message) {
            out.println(message);
        }

        private void cleanup() {
            try {
                if (playerId != -1) {
                    players.remove(playerId);
                    connectedClients.remove(playerId);
                    System.out.println("👋 Player #" + playerId + " disconnected. Connected: " + connectedClients.size());
                }
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Server-side Player wrapper
    public static class ServerPlayer {
        public int id;
        public String name;
        public int x, y;
        public int moveSpeed = 10;
        public Color color;
        public boolean alive = true;
        private final int spawnX;
        private final int spawnY;

        public ServerPlayer(int id, String name, int x, int y, Color color) {
            this.id = id;
            this.name = name;
            this.x = x;
            this.y = y;
            this.spawnX = x;
            this.spawnY = y;
            this.color = color;
        }

        public void move(int dx, int dy, TileType[][] map) {
            if (!alive) return;
            if (x + dx < 0 || x + dx >= map[0].length * MapPanel.TILE_SIZE ||
                y + dy < 0 || y + dy >= map.length * MapPanel.TILE_SIZE) {
                return;
            }

            int newX = x + dx;
            int newY = y + dy;

            // Simple collision check with corners
            int tileX = newX / MapPanel.TILE_SIZE;
            int tileY = newY / MapPanel.TILE_SIZE;
            int tileX1 = (newX + MapPanel.TILE_SIZE - 1) / MapPanel.TILE_SIZE;
            int tileY1 = (newY + MapPanel.TILE_SIZE - 1) / MapPanel.TILE_SIZE;

            if (map[tileY][tileX] != TileType.FLOOR || map[tileY][tileX1] != TileType.FLOOR ||
                map[tileY1][tileX] != TileType.FLOOR || map[tileY1][tileX1] != TileType.FLOOR) {
                return;
            }

            x = newX;
            y = newY;
        }

        public void reset() {
            x = spawnX;
            y = spawnY;
            alive = true;
        }
    }

    // Server-side Bomb wrapper
    public static class ServerBomb {
        public int tileX;
        public int tileY;
        public int remainingMs;
        public int radius;

        public ServerBomb(int tileX, int tileY, int fuseMs, int radius) {
            this.tileX = tileX;
            this.tileY = tileY;
            this.remainingMs = fuseMs;
            this.radius = radius;
        }

        public boolean tick(int deltaMs) {
            remainingMs -= deltaMs;
            return remainingMs <= 0;
        }
    }

    // Server-side Explosion wrapper
    public static class ServerExplosion {
        public List<int[]> tiles = new ArrayList<>();
        public int remainingMs = 700;

        public void tick(int deltaMs) {
            remainingMs -= deltaMs;
        }

        public boolean isExpired() {
            return remainingMs <= 0;
        }

        public void addTile(int tx, int ty) {
            tiles.add(new int[]{tx, ty});
        }
    }
}



