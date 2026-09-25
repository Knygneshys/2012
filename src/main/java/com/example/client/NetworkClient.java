package com.example.client;

import com.example.network.*;
import com.google.gson.Gson;
import java.io.*;
import java.net.Socket;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * Client-side network handler for connecting to the Bomberman server.
 */
public class NetworkClient {
    private String host;
    private int port;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private Gson gson = new Gson();

    private int playerId = -1;
    private Consumer<GameStateMessage> onGameStateUpdate;
    private Consumer<ServerResponseMessage> onServerResponse;
    private volatile boolean connected = false;
    private ExecutorService executorService;

    public NetworkClient(String host, int port) {
        this.host = host;
        this.port = port;
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public boolean connect() {
        try {
            socket = new Socket(host, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            connected = true;

            // Start listening for messages from server
            executorService.submit(this::listenForMessages);
            return true;
        } catch (IOException e) {
            System.err.println("❌ Failed to connect to server: " + e.getMessage());
            connected = false;
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
        String json = gson.toJson(msg);
        out.println(json);
    }

    private void listenForMessages() {
        try {
            String line;
            while (connected && (line = in.readLine()) != null) {
                handleServerMessage(line);
            }
        } catch (IOException e) {
            if (connected) {
                System.err.println("❌ Connection lost: " + e.getMessage());
            }
        } finally {
            connected = false;
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
                    System.out.println("✅ " + msg.message);
                } else if (!msg.success) {
                    System.err.println("❌ " + msg.message);
                }
                if (onServerResponse != null) {
                    onServerResponse.accept(msg);
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Error parsing message: " + e.getMessage());
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
        try {
            if (socket != null) {
                socket.close();
            }
            if (executorService != null) {
                executorService.shutdown();
                executorService.awaitTermination(2, TimeUnit.SECONDS);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}

