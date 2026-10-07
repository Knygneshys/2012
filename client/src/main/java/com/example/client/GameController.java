package com.example.client;

import com.example.client.network.NetworkClient;
import com.example.client.network.GameStateMessage;
import com.example.client.observer.MapStateObserver;
import com.example.client.observer.NpcStateObserver;
import com.example.client.observer.PlayerStateObserver;
import com.example.client.observer.PowerupStateObserver;

import java.awt.event.KeyEvent;
import javax.swing.SwingUtilities;

/**
 * Turns key presses into network messages, and wires the game's update
 * plumbing together.
 * <p>
 * What it deliberately no longer does is interpret a server update. It used to:
 * one fifty-line method re-parsed the tile grid, rebuilt the NPC and powerup
 * lists, and split the player list into local and remote, all because it was
 * the single listener the network layer allowed. That is the fan-out that
 * {@link GameStateSubject} and the observers now own, and what is left here is
 * the part that is genuinely this class's job — deciding which observers exist,
 * and turning keys into actions.
 * <p>
 * It is the composition root for the update path, which is why it is the one
 * place that names every observer. Adding a fifth observer is a line here and
 * nothing else.
 */
public class GameController {

    private final Player player;
    private final MapPanel mapPanel;
    private final NetworkClient networkClient;
    private final GameStateSubject gameState = new GameStateSubject();

    public GameController(Player player, MapPanel mapPanel, TileType[][] map, NetworkClient networkClient) {
        this.player = player;
        this.mapPanel = mapPanel;
        this.networkClient = networkClient;

        // Each observer is handed only what it needs and told about only one
        // slice of the update. The local player id is read through a supplier
        // because the server issues it after this constructor has run.
        gameState.subscribe(new MapStateObserver(mapPanel));
        gameState.subscribe(new NpcStateObserver(mapPanel));
        gameState.subscribe(new PowerupStateObserver(mapPanel));
        gameState.subscribe(new PlayerStateObserver(player, mapPanel, networkClient::getPlayerId));

        networkClient.setOnGameStateUpdate(this::onGameStateUpdate);

        mapPanel.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                int key = e.getKeyCode();
                switch (key) {
                    case KeyEvent.VK_W -> networkClient.sendPlayerInput("UP");
                    case KeyEvent.VK_S -> networkClient.sendPlayerInput("DOWN");
                    case KeyEvent.VK_A -> networkClient.sendPlayerInput("LEFT");
                    case KeyEvent.VK_D -> networkClient.sendPlayerInput("RIGHT");
                    case KeyEvent.VK_SPACE -> networkClient.sendPlayerInput("BOMB");
                    case KeyEvent.VK_R -> networkClient.sendPlayerInput("RESET");
                }
                mapPanel.repaint();
            }
        });
    }

    /**
     * Called when the server sends a game state update. It arrives on the socket
     * thread, so the subject publishes on the event dispatch thread that also
     * paints — which is what lets the observers touch Swing components directly.
     */
    private void onGameStateUpdate(GameStateMessage msg) {
        SwingUtilities.invokeLater(() -> gameState.publish(msg.gameState));
    }
}