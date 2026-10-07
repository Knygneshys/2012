package com.example.client.network;

import com.example.client.GameLogger;
import com.google.gson.Gson;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Client-side WebSocket network handler for connecting to the Bomberman server.
 * Uses Java's standard java.net.http.WebSocket.
 */
public class NetworkClient {
    private static final GameLogger log = GameLogger.getInstance();

    private final String host;
    private final int port;
    private final String path;
    private WebSocket webSocket;
    private final Gson gson = new Gson();

    private int playerId = -1;
    private Consumer<GameStateMessage> onGameStateUpdate;
    private Consumer<ServerResponseMessage> onServerResponse;
    private volatile boolean connected = false;

    public NetworkClient(String host, int port) {
        this(host, port, "/ws/game");
    }

    public NetworkClient(String host, int port, String path) {
        this.host = host;
        this.port = port;
        this.path = path.startsWith("/") ? path : "/" + path;
    }

    public boolean connect() {
        try {
            String wsUri;
            if (host.startsWith("ws://") || host.startsWith("wss://")) {
                wsUri = host;
            } else {
                wsUri = "ws://" + host + ":" + port + path;
            }

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .build();

            CompletableFuture<WebSocket> future = client.newWebSocketBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .buildAsync(URI.create(wsUri), new WebSocketListener());

            this.webSocket = future.get(5, TimeUnit.SECONDS);
            this.connected = true;
            log.info("Connected to WebSocket at " + wsUri);
            return true;
        } catch (Exception e) {
            log.error("Failed to connect to WebSocket", e);
            this.connected = false;
            return false;
        }
    }

    public void joinGame(String playerName) {
        if (!connected) {
            throw new IllegalStateException("Not connected to server");
        }
        PlayerJoinMessage msg = new PlayerJoinMessage(playerName, -1);
        sendMessage(msg);
    }

    public void sendPlayerInput(String action) {
        if (!connected || playerId == -1) {
            return;
        }
        PlayerInputMessage msg = new PlayerInputMessage(playerId, action);
        sendMessage(msg);
    }

    private void sendMessage(GameMessage msg) {
        if (webSocket != null && connected) {
            String json = gson.toJson(msg);
            webSocket.sendText(json, true);
        }
    }

    private class WebSocketListener implements WebSocket.Listener {
        private final StringBuilder buffer = new StringBuilder();

        @Override
        public void onOpen(WebSocket webSocket) {
            webSocket.request(1);
        }

        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            buffer.append(data);
            if (last) {
                String fullMessage = buffer.toString();
                buffer.setLength(0);
                handleServerMessage(fullMessage);
            }
            webSocket.request(1);
            return null;
        }

        @Override
        public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
            connected = false;
            log.info("WebSocket connection closed (" + statusCode + "): " + reason);
            return null;
        }

        @Override
        public void onError(WebSocket webSocket, Throwable error) {
            connected = false;
            log.error("WebSocket error", error);
        }
    }

    private void handleServerMessage(String json) {
        try {
            if (json.contains("\"messageType\":\"GAME_STATE\"")) {
                GameStateMessage msg = gson.fromJson(json, GameStateMessage.class);
                if (onGameStateUpdate != null) {
                    onGameStateUpdate.accept(msg);
                }
            } else if (json.contains("\"messageType\":\"SERVER_RESPONSE\"")) {
                ServerResponseMessage msg = gson.fromJson(json, ServerResponseMessage.class);
                if (msg.success && playerId == -1) {
                    playerId = msg.playerId;
                    log.info(msg.message);
                } else if (!msg.success) {
                    log.error(msg.message, null);
                }
                if (onServerResponse != null) {
                    onServerResponse.accept(msg);
                }
            }
        } catch (Exception e) {
            log.error("Error parsing message", e);
        }
    }

    public void setOnGameStateUpdate(Consumer<GameStateMessage> callback) {
        this.onGameStateUpdate = callback;
    }

    public void setOnServerResponse(Consumer<ServerResponseMessage> callback) {
        this.onServerResponse = callback;
    }

    public boolean isConnected() {
        return connected;
    }

    public int getPlayerId() {
        return playerId;
    }

    public void disconnect() {
        connected = false;
        if (webSocket != null) {
            try {
                webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "Client disconnect").join();
            } catch (Exception ignored) {
            }
        }
    }
}
