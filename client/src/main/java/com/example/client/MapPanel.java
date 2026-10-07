package com.example.client;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import javax.swing.JPanel;
import javax.swing.Timer;
import com.example.client.factories.MapElementFactory;
import com.example.client.factories.ClassicMapElementFactory;
import com.example.client.factories.IceMapElementFactory;

public class MapPanel extends JPanel {
    private final MapElementFactory mapElementFactory;
    public static final int TILE_SIZE = 40;
    private static final Color GRID_COLOR = new Color(30, 30, 30);

    private TileType[][] map;
    private Block[][] blocks;
    private final Player player; // Local player

    // Multiplayer support
    public final Map<Integer, RemotePlayer> remotePlayers = Collections.synchronizedMap(new HashMap<>());
    private final Map<Integer, NPC> npcs = new LinkedHashMap<>();
    private final List<Powerup> powerups = new ArrayList<>();
    private final Set<String> collectedPowerups = new HashSet<>();

    private final List<Bomb> bombs = new ArrayList<>();
    private final List<Explosion> explosions = new ArrayList<>();
    private List<int[]> serverBombs = new ArrayList<>(); // Bombs from server
    private final Timer timer;

    private static final int TICK_MS = 100;

    public MapPanel(TileType[][] map, Player player) {
        this.player = player;
        this.mapElementFactory = new IceMapElementFactory();
        setMap(map);
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

    /**
     * Adopts a new authoritative map and rebuilds the {@link Block} view of it.
     */
    private void setMap(TileType[][] map) {
        this.map = map;
        this.blocks = Block.fromMap(map, TILE_SIZE, mapElementFactory);
    }

    public TileType[][] getMap() {
        return map;
    }

    public Block[][] getBlocks() {
        return blocks;
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

    /**
     * Replaces the known NPCs with the ones the server currently simulates.
     */
    public void updateNpcs(List<NPC> snapshot) {
        Set<Integer> present = new HashSet<>();
        for (NPC incoming : snapshot) {
            present.add(incoming.id);
            NPC npc = npcs.get(incoming.id);
            if (npc == null) {
                npcs.put(incoming.id, incoming);
            } else {
                // keep the same instance so references stay valid between ticks
                npc.updateFromServer(incoming.getX(), incoming.getY(), incoming.alive);
            }
        }
        npcs.keySet().removeIf(id -> !present.contains(id));
    }

    /**
     * Replaces the visible powerups with the ones the server currently holds.
     */
    public void updatePowerups(List<Powerup> snapshot) {
        powerups.clear();
        powerups.addAll(snapshot);
    }

    public void updateGameState(TileType[][] newMap, List<int[][]> explosionTiles, List<int[]> bombTiles) {
        setMap(newMap);
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
        setMap(DemoMapFactory.createDefaultMap());
        bombs.clear();
        explosions.clear();
        remotePlayers.clear();
        npcs.clear();
        powerups.clear();
        collectedPowerups.clear();
        // reset player to spawn
        player.reset(1 * TILE_SIZE, 1 * TILE_SIZE);
    }

    public void placeBomb(Player p) {
        if (!p.alive) return; // don't allow placing when dead
        int px = p.getX();
        int py = p.getY();
        int tileX = (px + TILE_SIZE / 2) / TILE_SIZE;
        int tileY = (py + TILE_SIZE / 2) / TILE_SIZE;
        // can't place a bomb inside a wall
        if (!blocks[tileY][tileX].isPassable()) return;
        // don't place if a bomb already exists on that tile
        for (Bomb b : bombs) {
            if (b.tileX == tileX && b.tileY == tileY) return;
        }
        // respect the bomb limit the player currently has
        if (countBombsOf(p) >= p.maxBombs) return;
        bombs.add(new Bomb(tileX, tileY, Bomb.DEFAULT_FUSE_MS, p.bombRadius, p.id, TILE_SIZE));
    }

    private int countBombsOf(Player p) {
        int count = 0;
        for (Bomb b : bombs) {
            if (b.ownerId == p.id) {
                count++;
            }
        }
        return count;
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

        collectPowerups();
    }

    private void detonate(Bomb b) {
        Explosion ex = new Explosion();
        int bx = b.tileX;
        int by = b.tileY;
        // center
        ex.addTile(bx, by);
        // check player on center
        checkKills(bx, by);

        // four directions
        int[][] dirs = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        for (int[] d : dirs) {
            for (int step = 1; step <= b.radius; step++) {
                int nx = bx + d[0] * step;
                int ny = by + d[1] * step;
                if (nx < 0 || ny < 0 || ny >= blocks.length || nx >= blocks[0].length) break;

                Block block = blocks[ny][nx];
                if (!block.isPassable()) {
                    // The blast stops at any wall. A breakable wall is destroyed
                    // and leaves a passage behind, a solid wall just survives.
                    if (block.isDestructible()) {
                        ex.addTile(nx, ny);
                        destroyBlock(nx, ny, block);
                    }
                    break;
                }

                ex.addTile(nx, ny);
                // check characters caught by the blast
                checkKills(nx, ny);
            }
        }
        explosions.add(ex);
    }

    private void destroyBlock(int tileX, int tileY, Block block) {
        Block after = block.destroyed();
        blocks[tileY][tileX] = after;
        map[tileY][tileX] = after.getTileType();
    }

    private void checkKills(int tileX, int tileY) {
        if (player.alive && player.intersectsTile(tileX, tileY, TILE_SIZE)) {
            player.kill();
        }
        for (NPC npc : npcs.values()) {
            if (npc.alive && npc.intersectsTile(tileX, tileY, TILE_SIZE)) {
                npc.kill();
            }
        }
    }

    /**
     * Applies the bonus of every powerup the local player is standing on. The
     * server does the same for the authoritative stats, this keeps the client
     * side player in sync without waiting for the next state broadcast.
     */
    private void collectPowerups() {
        if (!player.alive) return;
        for (Powerup p : powerups) {
            String key = p.key();
            if (collectedPowerups.contains(key)) continue;
            if (player.overlaps(p)) {
                p.applyTo(player);
                collectedPowerups.add(key);
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Take stable copies of everything the network updates. State arrives
        // on the socket thread while this runs on the event dispatch thread,
        // and iterating a collection that changes mid paint aborts the frame.
        Block[][] frameBlocks = blocks;
        List<int[]> frameServerBombs = serverBombs;
        List<Explosion> frameExplosions = new ArrayList<>(explosions);
        List<Powerup> framePowerups = new ArrayList<>(powerups);
        List<NPC> frameNpcs = new ArrayList<>(npcs.values());
        List<Bomb> frameBombs = new ArrayList<>(bombs);

        for (int y = 0; y < frameBlocks.length; y++) {
            for (int x = 0; x < frameBlocks[0].length; x++) {
                Block block = frameBlocks[y][x];
                g.setColor(block.color());
                g.fillRect(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                g.setColor(GRID_COLOR);
                g.drawRect(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            }
        }

        // draw bombs
        for (Bomb b : frameBombs) {
            int bx = b.tileX * TILE_SIZE;
            int by = b.tileY * TILE_SIZE;
            g.setColor(Color.BLACK);
            int pad = TILE_SIZE / 6;
            g.fillOval(bx + pad, by + pad, TILE_SIZE - pad * 2, TILE_SIZE - pad * 2);
        }

        // draw bombs from server
        for (int[] bomb : frameServerBombs) {
            int bx = bomb[0] * TILE_SIZE;
            int by = bomb[1] * TILE_SIZE;
            g.setColor(Color.BLACK);
            int pad = TILE_SIZE / 6;
            g.fillOval(bx + pad, by + pad, TILE_SIZE - pad * 2, TILE_SIZE - pad * 2);
        }

        // draw powerups
        for (Powerup p : framePowerups) {
            g.setColor(p.kind.color());
            int pad = TILE_SIZE / 4;
            g.fillRoundRect(p.getX() + pad, p.getY() + pad, TILE_SIZE - pad * 2, TILE_SIZE - pad * 2, 8, 8);
            g.setColor(Color.WHITE);
            g.drawString(p.kind.label(), p.getX() + pad + 2, p.getY() + TILE_SIZE - pad - 3);
        }

        // draw explosions
        for (Explosion ex : frameExplosions) {
            g.setColor(new Color(255, 140, 0));
            for (int[] t : ex.tiles) {
                int tx = t[0] * TILE_SIZE;
                int ty = t[1] * TILE_SIZE;
                g.fillRect(tx, ty, TILE_SIZE, TILE_SIZE);
            }
        }

        // Draw local player
        int playerX = player.getX();
        int playerY = player.getY();
        g.setColor(player.alive ? player.color : Character.DEAD_COLOR);
        g.fillOval(playerX, playerY, TILE_SIZE, TILE_SIZE);

        // Draw player name label
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 10));
        g2.drawString(player.name, playerX, playerY - 2);
        g2.dispose();

        // Draw remote players
        List<RemotePlayer> frameRemotePlayers = new ArrayList<>(remotePlayers.values());
        for (RemotePlayer rp : frameRemotePlayers) {
            g.setColor(rp.alive ? rp.color : Character.DEAD_COLOR);
            g.fillOval(rp.x, rp.y, TILE_SIZE, TILE_SIZE);

            // Draw player name label
            g2 = (Graphics2D) g.create();
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("SansSerif", Font.BOLD, 10));
            g2.drawString(rp.name, rp.x, rp.y - 2);
            g2.dispose();
        }

        // Draw NPCs
        for (NPC npc : frameNpcs) {
            if (!npc.alive) continue;
            int nx = npc.getX();
            int ny = npc.getY();
            int pad = TILE_SIZE / 6;
            g.setColor(npc.color);
            g.fillOval(nx + pad, ny + pad, TILE_SIZE - pad * 2, TILE_SIZE - pad * 2);
            g.setColor(Color.BLACK);
            g.fillRect(nx + TILE_SIZE / 3, ny + TILE_SIZE / 3, 4, 4);
            g.fillRect(nx + TILE_SIZE / 2 + 2, ny + TILE_SIZE / 3, 4, 4);

            g2 = (Graphics2D) g.create();
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("SansSerif", Font.BOLD, 10));
            g2.drawString(npc.name, nx, ny - 2);
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