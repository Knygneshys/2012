package com.example;

import com.example.client.NetworkClient;
import com.example.network.GameStateMessage;
import com.example.network.GameStateData;
import java.awt.event.KeyEvent;
import java.awt.Color;

public class GameController {
    private final Player player;
    private final MapPanel mapPanel;
    private final NetworkClient networkClient;

    public GameController(Player player, MapPanel mapPanel, TileType[][] map, NetworkClient networkClient) {
        this.player = player;
        this.mapPanel = mapPanel;
        this.networkClient = networkClient;

        // Set up game state update callback
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
     * Called when the server sends a game state update.
     */
    private void onGameStateUpdate(GameStateMessage msg) {
        GameStateData state = msg.gameState;

        // Convert map from int array back to TileType
        TileType[][] map = new TileType[state.map.length][state.map[0].length];
        for (int y = 0; y < state.map.length; y++) {
            for (int x = 0; x < state.map[0].length; x++) {
                map[y][x] = TileType.values()[state.map[y][x]];
            }
        }

        // Update players
        for (GameStateData.PlayerData pd : state.players) {
            if (pd.playerId == networkClient.getPlayerId()) {
                // Update our local player
                player.position = new GameObject.Position(pd.x, pd.y);
                player.alive = pd.alive;
                player.moveSpeed = pd.moveSpeed;
                player.color = hexToColor(pd.colorHex);
            } else {
                // Update or add remote player
                MapPanel.RemotePlayer rp = mapPanel.remotePlayers.getOrDefault(pd.playerId, null);
                if (rp == null) {
                    Color color = hexToColor(pd.colorHex);
                    mapPanel.addRemotePlayer(pd.playerId, pd.name, color);
                }
                mapPanel.updateRemotePlayer(pd.playerId, pd.x, pd.y, pd.alive);
            }
        }

        // Update map and explosions
        java.util.List<int[][]> explosionTiles = new java.util.ArrayList<>();
        for (GameStateData.ExplosionData ed : state.explosions) {
            explosionTiles.add(ed.tiles);
        }

        // Convert bombs to tile coordinates
        java.util.List<int[]> bombTiles = new java.util.ArrayList<>();
        for (GameStateData.BombData bd : state.bombs) {
            bombTiles.add(new int[]{bd.tileX, bd.tileY});
        }

        mapPanel.updateGameState(map, explosionTiles, bombTiles);

        mapPanel.repaint();
    }

    private Color hexToColor(String hex) {
        hex = hex.replace("#", "");
        int r = Integer.parseInt(hex.substring(0, 2), 16);
        int g = Integer.parseInt(hex.substring(2, 4), 16);
        int b = Integer.parseInt(hex.substring(4, 6), 16);
        return new Color(r, g, b);
    }
}
