package com.example.server.network;

import com.example.server.GameServer;
import com.google.gson.Gson;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class GameWebSocketHandler extends TextWebSocketHandler {

    private final GameServer gameServer;
    private final Gson gson = new Gson();

    public GameWebSocketHandler(@Lazy GameServer gameServer) {
        this.gameServer = gameServer;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        System.out.println("[Server] WebSocket connection established from " + session.getRemoteAddress() + " (session " + session.getId() + ")");
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        String payload = message.getPayload();
        try {
            if (payload.contains("\"messageType\":\"PLAYER_JOIN\"")) {
                PlayerJoinMessage msg = gson.fromJson(payload, PlayerJoinMessage.class);
                gameServer.handlePlayerJoin(session, msg);
            } else if (payload.contains("\"messageType\":\"PLAYER_INPUT\"")) {
                PlayerInputMessage msg = gson.fromJson(payload, PlayerInputMessage.class);
                gameServer.handlePlayerInput(session, msg);
            }
        } catch (Exception e) {
            System.err.println("[Server] Error handling WebSocket message: " + e.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        gameServer.handleSessionClosed(session);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        System.err.println("[Server] Transport error for session " + session.getId() + ": " + exception.getMessage());
        gameServer.handleSessionClosed(session);
    }
}
