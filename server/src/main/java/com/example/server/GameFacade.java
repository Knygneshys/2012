package com.example.server;

import com.example.server.network.PlayerInputMessage;
import com.example.server.network.PlayerJoinMessage;
import com.google.gson.Gson;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Entry point for server game requests and status queries. Facade between server's entry points (GameWebSocketHandler / ServerStatusController) and game logic (GameServer) */
@Component
public class GameFacade {

    private final GameServer gameServer;
    private final Gson gson = new Gson();

    public GameFacade(GameServer gameServer) {
        this.gameServer = gameServer;
    }

    public void handleMessage(WebSocketSession session, String payload) {
        if (payload.contains("\"messageType\":\"PLAYER_JOIN\"")) {
            gameServer.handlePlayerJoin(session, gson.fromJson(payload, PlayerJoinMessage.class));
        } else if (payload.contains("\"messageType\":\"PLAYER_INPUT\"")) {
            gameServer.handlePlayerInput(session, gson.fromJson(payload, PlayerInputMessage.class));
        }
    }

    public void disconnect(WebSocketSession session) {
        gameServer.handleSessionClosed(session);
    }

    public Map<String, Object> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("game", "Bomberman Server");
        status.put("running", gameServer.isRunning());
        status.put("port", gameServer.getPort());
        status.put("wsEndpoint", "/ws/game");
        status.put("connectedPlayers", gameServer.getConnectedPlayerCount());
        status.put("maxPlayers", gameServer.getMaxPlayers());
        return status;
    }

    public List<Map<String, Object>> getPlayers() {
        return gameServer.getPlayerSummaryList();
    }
}
