package com.example.server.proxy;

import com.example.server.GameConstants;
import com.example.server.GameLogger;
import com.example.server.GameServer;
import com.example.server.GameService;
import com.example.server.network.AccessControlMessage;
import com.example.server.network.PlayerInputMessage;
import com.example.server.network.PlayerJoinMessage;
import com.google.gson.Gson;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Proxy pattern — Protection Proxy.
 * <p>
 * Implements the same {@link GameService} interface as the real
 * {@link GameServer} and stands in front of it, so {@link com.example.server.GameFacade}
 * cannot tell which one it is talking to. Every player request passes through
 * here first and is only forwarded when the player is allowed to make it:
 * <ul>
 *   <li><b>Host-only actions</b> — the first player to join is the host, and only
 *       the host may {@code START} the game from the lobby, {@code RESET} the
 *       round, or send everyone back to the {@code LOBBY}. When the host leaves, the role
 *       passes to the player who joined next.</li>
 *   <li><b>Rate limiting</b> — every move message moves a player by its full
 *       speed, so a client sending messages faster would move faster. Moves
 *       arriving within {@link GameConstants#MIN_MOVE_INTERVAL_MS} of the
 *       previous one are dropped.</li>
 *   <li><b>Spectators</b> — dead players' inputs are dropped (except a host's
 *       reset, so a dead host can still start the next round).</li>
 * </ul>
 * The server console ({@link com.example.server.interpreter.ConsoleInterpreter})
 * talks to {@link GameServer} directly: the operator is trusted and is not a
 * player, so it deliberately bypasses these checks.
 */
@Component
@Primary
public class GameServerProxy implements GameService {

    private static final GameLogger log = GameLogger.getInstance();

    /** Actions only the host may perform, with the explanation sent to anyone else. */
    private static final Map<String, String> HOST_ONLY_ACTIONS = Map.of(
            "RESET", "restart the round",
            "START", "start the game",
            "LOBBY", "end the round");
    private static final Set<String> MOVE_ACTIONS = Set.of("UP", "DOWN", "LEFT", "RIGHT");

    private final GameServer realServer;
    private final Gson gson = new Gson();

    /** Sessions of joined players, so host changes can be announced to all of them. */
    private final Map<Integer, WebSocketSession> members = new ConcurrentHashMap<>();
    private final Map<Integer, Long> lastMoveAt = new ConcurrentHashMap<>();
    private volatile int hostPlayerId = -1;

    public GameServerProxy(GameServer realServer) {
        this.realServer = realServer;
    }

    @Override
    public synchronized void handlePlayerJoin(WebSocketSession session, PlayerJoinMessage msg) {
        realServer.handlePlayerJoin(session, msg);

        Integer playerId = realServer.getPlayerIdForSession(session.getId());
        if (playerId == null) {
            return; // join was refused (game full); nothing to track
        }
        members.put(playerId, session);
        if (hostPlayerId == -1) {
            hostPlayerId = playerId;
            log.info("[Proxy] Player #" + playerId + " is now the host");
        }
        broadcastHost("Player #" + hostPlayerId + " is the host");
    }

    @Override
    public void handlePlayerInput(WebSocketSession session, PlayerInputMessage msg) {
        Integer playerId = realServer.getPlayerIdForSession(session.getId());
        if (playerId == null || msg.action == null) {
            return;
        }
        String action = msg.action;

        if (HOST_ONLY_ACTIONS.containsKey(action)) {
            if (playerId != hostPlayerId) {
                log.info("[Proxy] Denied " + action + " from Player #" + playerId + " (not host)");
                send(session, AccessControlMessage.denied(hostPlayerId, action,
                        "Only the host (Player #" + hostPlayerId + ") can " + HOST_ONLY_ACTIONS.get(action)));
                return;
            }
            realServer.handlePlayerInput(session, msg);
            return;
        }

        if (!realServer.isPlayerAlive(playerId)) {
            return; // spectators can watch but not act
        }

        if (MOVE_ACTIONS.contains(action) && !allowMove(playerId)) {
            return; // too soon after the previous move — silently dropped
        }

        realServer.handlePlayerInput(session, msg);
    }

    @Override
    public synchronized void handleSessionClosed(WebSocketSession session) {
        Integer playerId = realServer.getPlayerIdForSession(session.getId());
        realServer.handleSessionClosed(session);
        if (playerId == null) {
            return;
        }
        members.remove(playerId);
        lastMoveAt.remove(playerId);

        if (playerId == hostPlayerId) {
            List<Integer> remaining = realServer.getPlayerIds();
            hostPlayerId = remaining.isEmpty() ? -1 : remaining.get(0);
            if (hostPlayerId != -1) {
                log.info("[Proxy] Host left; Player #" + hostPlayerId + " is the new host");
                broadcastHost("The host left. Player #" + hostPlayerId + " is the new host");
            }
        }
    }

    /** Moves are allowed at most once every MIN_MOVE_INTERVAL_MS per player. */
    private boolean allowMove(int playerId) {
        long now = System.currentTimeMillis();
        Long previous = lastMoveAt.get(playerId);
        if (previous != null && now - previous < GameConstants.MIN_MOVE_INTERVAL_MS) {
            return false;
        }
        lastMoveAt.put(playerId, now);
        return true;
    }

    public int getHostPlayerId() {
        return hostPlayerId;
    }

    private void broadcastHost(String message) {
        AccessControlMessage update = AccessControlMessage.hostUpdate(hostPlayerId, message);
        for (WebSocketSession s : new ArrayList<>(members.values())) {
            send(s, update);
        }
    }

    private void send(WebSocketSession session, AccessControlMessage message) {
        if (session == null || !session.isOpen()) {
            return;
        }
        try {
            // Same lock GameServer uses: a WebSocket session allows one writer at a time.
            synchronized (session) {
                session.sendMessage(new TextMessage(gson.toJson(message)));
            }
        } catch (IOException | IllegalStateException e) {
            // IllegalStateException: the session closed between isOpen() and the send,
            // e.g. two players leaving at the same moment. Nothing left to tell it.
            log.info("[Proxy] Could not notify session " + session.getId() + ": " + e.getMessage());
        }
    }

    // ---- Requests with no access rules are forwarded unchanged ----

    @Override
    public void resetGame() {
        realServer.resetGame();
    }

    @Override
    public void startGame() {
        realServer.startGame();
    }

    @Override
    public void returnToLobby() {
        realServer.returnToLobby();
    }

    @Override
    public com.example.server.GamePhase getPhase() {
        return realServer.getPhase();
    }

    @Override
    public Integer getPlayerIdForSession(String sessionId) {
        return realServer.getPlayerIdForSession(sessionId);
    }

    @Override
    public List<Integer> getPlayerIds() {
        return realServer.getPlayerIds();
    }

    @Override
    public boolean isPlayerAlive(int playerId) {
        return realServer.isPlayerAlive(playerId);
    }

    @Override
    public boolean isRunning() {
        return realServer.isRunning();
    }

    @Override
    public int getPort() {
        return realServer.getPort();
    }

    @Override
    public int getConnectedPlayerCount() {
        return realServer.getConnectedPlayerCount();
    }

    @Override
    public int getMaxPlayers() {
        return realServer.getMaxPlayers();
    }

    @Override
    public List<Map<String, Object>> getPlayerSummaryList() {
        List<Map<String, Object>> list = realServer.getPlayerSummaryList();
        for (Map<String, Object> info : list) {
            info.put("host", Integer.valueOf(hostPlayerId).equals(info.get("id")));
        }
        return list;
    }
}
