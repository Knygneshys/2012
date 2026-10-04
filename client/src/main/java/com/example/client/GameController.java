package com.example.client;

import com.example.client.network.NetworkClient;
import com.example.client.network.GameStateMessage;
import com.example.client.network.GameStateData;
import java.awt.event.KeyEvent;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingUtilities;

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
     * Called when the server sends a game state update. It arrives on the socket
     * thread, so the panel and player models are updated on the event dispatch
     * thread that also paints them.
     */
    private void onGameStateUpdate(GameStateMessage msg) {
        SwingUtilities.invokeLater(() -> applyGameState(msg.gameState));
    }

    private void applyGameState(GameStateData state) {
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
                player.id = pd.playerId;
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
        List<int[][]> explosionTiles = new ArrayList<>();
        for (GameStateData.ExplosionData ed : state.explosions) {
            explosionTiles.add(ed.tiles);
        }

        // Convert bombs to tile coordinates
        List<int[]> bombTiles = new ArrayList<>();
        for (GameStateData.BombData bd : state.bombs) {
            bombTiles.add(new int[]{bd.tileX, bd.tileY});
        }

        mapPanel.updateGameState(map, explosionTiles, bombTiles);
        mapPanel.updateNpcs(buildNpcs(state.npcs));
        mapPanel.updatePowerups(buildPowerups(state.powerups));

        mapPanel.repaint();
    }

    private List<NPC> buildNpcs(List<GameStateData.NpcData> data) {
        List<NPC> npcList = new ArrayList<>();
        for (GameStateData.NpcData nd : data) {
            // alive and moveSpeed come from the server, the client never
            // decides them itself
            npcList.add(new NPC(nd.npcId, "NPC " + nd.npcId, nd.x, nd.y,
                    nd.moveSpeed, hexToColor(nd.colorHex),
                    MapPanel.TILE_SIZE, MapPanel.TILE_SIZE, nd.alive));
        }
        return npcList;
    }

    private List<Powerup> buildPowerups(List<GameStateData.PowerupData> data) {
        List<Powerup> powerupList = new ArrayList<>();
        for (GameStateData.PowerupData pd : data) {
            powerupList.add(new Powerup(Powerup.Kind.fromOrdinal(pd.kind),
                    pd.tileX, pd.tileY, MapPanel.TILE_SIZE));
        }
        return powerupList;
    }

    private Color hexToColor(String hex) {
        hex = hex.replace("#", "");
        int r = Integer.parseInt(hex.substring(0, 2), 16);
        int g = Integer.parseInt(hex.substring(2, 4), 16);
        int b = Integer.parseInt(hex.substring(4, 6), 16);
        return new Color(r, g, b);
    }
}
