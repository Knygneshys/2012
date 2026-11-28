package com.example.server;

import com.example.server.network.PlayerInputMessage;
import com.example.server.network.PlayerJoinMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;
import java.util.Map;

/**
 * Proxy pattern — Subject.
 * <p>
 * The operations the outside world (through {@link GameFacade}) may perform on
 * the game. {@link GameServer} is the real subject that does the work;
 * {@link com.example.server.proxy.GameServerProxy} implements the same
 * interface and decides whether a request is allowed through to it.
 */
public interface GameService {

    void handlePlayerJoin(WebSocketSession session, PlayerJoinMessage msg);

    void handlePlayerInput(WebSocketSession session, PlayerInputMessage msg);

    void handleSessionClosed(WebSocketSession session);

    void resetGame();

    void startGame();

    void returnToLobby();

    GamePhase getPhase();

    /** The player id a session was given on join, or null if it has none. */
    Integer getPlayerIdForSession(String sessionId);

    /** Ids of every player currently in the game, in join order. */
    List<Integer> getPlayerIds();

    boolean isPlayerAlive(int playerId);

    boolean isRunning();

    int getPort();

    int getConnectedPlayerCount();

    int getMaxPlayers();

    List<Map<String, Object>> getPlayerSummaryList();
}
