package com.example.server.network;

import com.example.server.GameLogger;
import com.example.server.GameFacade;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class GameWebSocketHandler extends TextWebSocketHandler {

    private static final GameLogger log = GameLogger.getInstance();

    private final GameFacade gameFacade;

    public GameWebSocketHandler(GameFacade gameFacade) {
        this.gameFacade = gameFacade;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("WebSocket connection established from " + session.getRemoteAddress() + " (session " + session.getId() + ")");
    }

    @Override
    protected void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) {
        try {
            gameFacade.handleMessage(session, message.getPayload());
        } catch (Exception e) {
            log.error("Error handling WebSocket message", e);
        }
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) {
        gameFacade.disconnect(session);
    }

    @Override
    public void handleTransportError(WebSocketSession session, @NonNull Throwable exception) {
        log.error("Transport error for session " + session.getId(), exception);
        gameFacade.disconnect(session);
    }
}
