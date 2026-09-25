package com.example;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import javax.swing.JPanel;
import javax.swing.Timer;

public class MapPanel extends JPanel {
    public static final int TILE_SIZE = 40;
    private static final Color GRID_COLOR = new Color(30, 30, 30);
    private static final Color FLOOR_COLOR = new Color(205, 190, 160);
    private static final Color HARD_WALL_COLOR = new Color(70, 90, 120);
    private static final Color SOFT_BLOCK_COLOR = new Color(160, 110, 70);

    private TileType[][] map;
    private final Player player; // Local player

    // Multiplayer support
    public final Map<Integer, RemotePlayer> remotePlayers = Collections.synchronizedMap(new HashMap<>());

    private final List<Bomb> bombs = new ArrayList<>();
    private final List<Explosion> explosions = new ArrayList<>();
    private List<int[]> serverBombs = new ArrayList<>(); // Bombs from server
    private final Timer timer;

    private static final int TICK_MS = 100;

    public MapPanel(TileType[][] map, Player player) {
        this.map = map;
        this.player = player;
        setPreferredSize(new Dimension(
                DemoMapFactory.MAP_WIDTH * TILE_SIZE,
                DemoMapFactory.MAP_HEIGHT * TILE_SIZE
        ));

        // Timer to update bombs/explosions
        timer = new Timer(TICK_MS, e -> {
            tick(TICK_MS);
            repaint();
        });
        timer.start();
    }

    public TileType[][] getMap() {
        return map;
    }

    public void addRemotePlayer(int playerId, String name, Color color) {
        RemotePlayer rp = new RemotePlayer(playerId, name, color);
        remotePlayers.put(playerId, rp);
    }

    public void updateRemotePlayer(int playerId, int x, int y, boolean alive) {
        RemotePlayer rp = remotePlayers.get(playerId);
        if (rp != null) {
            rp.x = x;
            rp.y = y;
            rp.alive = alive;
        }
    }

    public void updateGameState(TileType[][] newMap, List<int[][]> explosionTiles, List<int[]> bombTiles) {
        this.map = newMap;
        this.explosions.clear();
        this.serverBombs = bombTiles;
        for (int[][] tiles : explosionTiles) {
            Explosion ex = new Explosion();
            for (int[] tile : tiles) {
                ex.addTile(tile[0], tile[1]);
            }
            this.explosions.add(ex);
        }
    }

    public void resetGame() {
        // regenerate map
        this.map = DemoMapFactory.createDefaultMap();
        bombs.clear();
        explosions.clear();
        remotePlayers.clear();
        // reset player to spawn
        player.position = new GameObject.Position(1 * TILE_SIZE, 1 * TILE_SIZE);
        player.alive = true;
        player.color = player.initialColor;
        player.moveSpeed = player.initialMoveSpeed;
    }

    public void placeBomb(Player p) {
        if (!p.alive) return; // don't allow placing when dead
        int px = p.getPosition().x();
        int py = p.getPosition().y();
        int tileX = (px + TILE_SIZE/2) / TILE_SIZE;
        int tileY = (py + TILE_SIZE/2) / TILE_SIZE;
        // don't place if a bomb already exists on that tile
        for (Bomb b : bombs) {
            if (b.tileX == tileX && b.tileY == tileY) return;
        }
        // one bomb allowed per player for now
        bombs.add(new Bomb(tileX, tileY, 2000, 2));
    }

    private void tick(int deltaMs) {
        // bombs
        List<Bomb> detonated = new ArrayList<>();
        for (Bomb b : bombs) {
            if (b.tick(deltaMs)) {
                detonated.add(b);
            }
        }
        for (Bomb b : detonated) {
            detonate(b);
            bombs.remove(b);
        }

        // explosions
        Iterator<Explosion> it = explosions.iterator();
        while (it.hasNext()) {
            Explosion ex = it.next();
            ex.tick(deltaMs);
            if (ex.isExpired()) {
                it.remove();
            }
        }
    }

    private void detonate(Bomb b) {
        Explosion ex = new Explosion();
        int bx = b.tileX;
        int by = b.tileY;
        // center
        ex.addTile(bx, by);
        // check player on center
        checkPlayerKill(bx, by);

        // four directions
        int[][] dirs = {{1,0},{-1,0},{0,1},{0,-1}};
        for (int[] d : dirs) {
            for (int step = 1; step <= b.radius; step++) {
                int nx = bx + d[0]*step;
                int ny = by + d[1]*step;
                if (nx < 0 || ny < 0 || ny >= map.length || nx >= map[0].length) break;
                if (map[ny][nx] == TileType.HARD_WALL) break;
                ex.addTile(nx, ny);
                // destroy soft block and stop propagation
                if (map[ny][nx] == TileType.SOFT_BLOCK) {
                    map[ny][nx] = TileType.FLOOR;
                    break;
                }
                // check player
                checkPlayerKill(nx, ny);
            }
        }
        explosions.add(ex);
    }

    private void checkPlayerKill(int tileX, int tileY) {
        int playerTileX = player.getPosition().x() / TILE_SIZE;
        int playerTileY = player.getPosition().y() / TILE_SIZE;
        if (playerTileX == tileX && playerTileY == tileY) {
            player.alive = false;
            player.color = Color.GRAY;
            player.moveSpeed = 0;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (int y = 0; y < DemoMapFactory.MAP_HEIGHT; y++) {
            for (int x = 0; x < DemoMapFactory.MAP_WIDTH; x++) {
                g.setColor(tileColor(map[y][x]));
                g.fillRect(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                g.setColor(GRID_COLOR);
                g.drawRect(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            }
        }

        // draw bombs
        for (Bomb b : bombs) {
            int bx = b.tileX * TILE_SIZE;
            int by = b.tileY * TILE_SIZE;
            g.setColor(Color.BLACK);
            int pad = TILE_SIZE/6;
            g.fillOval(bx + pad, by + pad, TILE_SIZE - pad*2, TILE_SIZE - pad*2);
        }

        // draw bombs from server
        for (int[] bomb : serverBombs) {
            int bx = bomb[0] * TILE_SIZE;
            int by = bomb[1] * TILE_SIZE;
            g.setColor(Color.BLACK);
            int pad = TILE_SIZE/6;
            g.fillOval(bx + pad, by + pad, TILE_SIZE - pad*2, TILE_SIZE - pad*2);
        }

        // draw explosions
        for (Explosion ex : explosions) {
            g.setColor(new Color(255, 140, 0));
            for (int[] t : ex.tiles) {
                int tx = t[0] * TILE_SIZE;
                int ty = t[1] * TILE_SIZE;
                g.fillRect(tx, ty, TILE_SIZE, TILE_SIZE);
            }
        }

        // Draw local player
        int playerX = player.getPosition().x();
        int playerY = player.getPosition().y();
        g.setColor(player.color);
        g.fillOval(playerX, playerY, TILE_SIZE, TILE_SIZE);

        // Draw player name label
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 10));
        g2.drawString(player.name, playerX, playerY - 2);
        g2.dispose();

        // Draw remote players
        for (RemotePlayer rp : remotePlayers.values()) {
            g.setColor(rp.color);
            g.fillOval(rp.x, rp.y, TILE_SIZE, TILE_SIZE);

            // Draw player name label
            g2 = (Graphics2D) g.create();
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("SansSerif", Font.BOLD, 10));
            g2.drawString(rp.name, rp.x, rp.y - 2);
            g2.dispose();
        }

        // if dead, draw retry overlay
        if (!player.alive) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setColor(new Color(0, 0, 0, 160));
            g2d.fillRect(0, 0, getWidth(), getHeight());
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("SansSerif", Font.BOLD, 28));
            String msg = "You Died";
            String hint = "Press R to Retry";
            int mw = g2d.getFontMetrics().stringWidth(msg);
            int hw = g2d.getFontMetrics().stringWidth(hint);
            g2d.drawString(msg, (getWidth() - mw) / 2, getHeight() / 2 - 10);
            g2d.drawString(hint, (getWidth() - hw) / 2, getHeight() / 2 + 30);
            g2d.dispose();
        }
    }

    private Color tileColor(TileType tile) {
        return switch (tile) {
            case FLOOR -> FLOOR_COLOR;
            case HARD_WALL -> HARD_WALL_COLOR;
            case SOFT_BLOCK -> SOFT_BLOCK_COLOR;
        };
    }

    /**
     * Represents a remote player on the network.
     */
    public static class RemotePlayer {
        public int id;
        public String name;
        public int x;
        public int y;
        public boolean alive;
        public Color color;

        public RemotePlayer(int id, String name, Color color) {
            this.id = id;
            this.name = name;
            this.color = color;
            this.alive = true;
        }
    }
}

