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
import com.example.client.bridge.MapElementFactoryBridge;
import com.example.client.bridge.Theme;

public class MapPanel extends JPanel {
    private MapElementFactory mapElementFactory;
    private TileAppearanceFactory tileAppearances;
    private Theme theme;
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
    private final List<Decoration> decorations = new ArrayList<>();
    private List<int[]> serverBombs = new ArrayList<>(); // Bombs from server
    private final Timer timer;

    private static final int TICK_MS = 100;
    private static final int NOTICE_MS = 2500;
    /** Server bomb fuse, used to scale the blast preview's urgency. */
    private static final int FUSE_MS = 2000;

    // Host / access-control info from the server's protection proxy
    private volatile int hostPlayerId = -1;
    private volatile int localPlayerId = -1;
    private volatile String notice;
    private volatile long noticeUntil;

    // Lobby (set by HudStateObserver)
    private volatile boolean lobby;
    private volatile List<String> lobbyPlayers = List.of();

    private final CharacterSprite sprites = new CharacterSprite();
    private InGameMenu menu;

    public MapPanel(TileType[][] map, Player player) {
        this.player = player;
        this.map = map;
        applyTheme(Theme.loadSaved());
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

    private void generateDecorations() {
        decorations.clear();

        Decoration prototype = mapElementFactory.createDecoration();

        for (int y = 1; y < map.length - 1; y++) {
            for (int x = 1; x < map[y].length - 1; x++) {

                if (map[y][x] != TileType.FLOOR) {
                    continue;
                }

                if ((x * 17 + y * 11) % 13 != 0) {
                    continue;
                }

                Decoration decoration = prototype.clone(false);
                decoration.setPosition(x, y);
                decorations.add(decoration);
            }
        }
    }


    /** Updates who the host is, so the host's name gets a star and the death overlay shows the right hint. */
    public void setHostInfo(int hostPlayerId, int localPlayerId) {
        this.hostPlayerId = hostPlayerId;
        this.localPlayerId = localPlayerId;
        repaint();
    }

    /** Shows a short banner at the top of the map, e.g. when the server refuses an action. */
    public void showNotice(String text) {
        this.notice = text;
        this.noticeUntil = System.currentTimeMillis() + NOTICE_MS;
        repaint();
    }

    /** Switches the lobby overlay on or off; {@code playerLabels} are the names listed in it. */
    public void setLobby(boolean lobby, List<String> playerLabels) {
        this.lobby = lobby;
        this.lobbyPlayers = List.copyOf(playerLabels);
    }

    public boolean isLobby() {
        return lobby;
    }

    /** The Esc menu to draw on top of everything else. */
    public void setMenu(InGameMenu menu) {
        this.menu = menu;
    }

    public Theme getTheme() {
        return theme;
    }

    /** Switches to the next (dir = 1) or previous (dir = -1) theme and remembers it. */
    public void cycleTheme(int dir) {
        applyTheme(theme.step(dir));
        theme.save();
        repaint();
    }

    /**
     * Bridge pattern: swapping the {@link com.example.client.bridge.TileTheme}
     * behind the bridge re-skins the whole map; walls, floor, decorations and
     * the cached tile colours are rebuilt from it.
     */
    private void applyTheme(Theme newTheme) {
        this.theme = newTheme;
        this.mapElementFactory = new MapElementFactoryBridge(newTheme.create());
        this.tileAppearances = new TileAppearanceFactory(mapElementFactory);
        setMap(map);
        generateDecorations();
    }

    /** Stops the repaint timer once this panel is no longer shown. */
    public void stop() {
        timer.stop();
    }

    private boolean isLocalHost() {
        return localPlayerId != -1 && localPlayerId == hostPlayerId;
    }

    private String labelFor(String name, int playerId) {
        return playerId != -1 && playerId == hostPlayerId ? "\u2605 " + name : name;
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
        generateDecorations();
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


    private void drawDecorations(Graphics g) {
        for (Decoration decoration : decorations) {
            int x = decoration.getX() * TILE_SIZE;
            int y = decoration.getY() * TILE_SIZE;

            DecorationStyle style = decoration.getStyle();

            if (style.getName().equals("BARREL")) {
                // Barrel body
                g.setColor(style.getColor());
                g.fillRect(x + 10, y + 7, 20, 26);

                // Metal bands
                g.setColor(Color.DARK_GRAY);
                g.fillRect(x + 10, y + 11, 20, 4);
                g.fillRect(x + 10, y + 25, 20, 4);

                // Outline
                g.setColor(Color.BLACK);
                g.drawRect(x + 10, y + 7, 20, 26);

            } else if (style.getName().equals("CACTUS")) {
                g.setColor(style.getColor());
                g.fillRoundRect(x + 16, y + 6, 8, 30, 8, 8);      // trunk
                g.fillRoundRect(x + 8, y + 14, 6, 12, 6, 6);      // left arm
                g.fillRect(x + 8, y + 22, 10, 4);
                g.fillRoundRect(x + 26, y + 10, 6, 12, 6, 6);     // right arm
                g.fillRect(x + 22, y + 18, 10, 4);
                g.setColor(style.getColor().darker());
                g.drawLine(x + 20, y + 9, x + 20, y + 33);

            } else if (style.getName().equals("SNOWMAN")) {
                // Body
                g.setColor(style.getColor());
                g.fillOval(x + 10, y + 18, 20, 20);

                // Head
                g.fillOval(x + 14, y + 7, 12, 14);

                // Eyes
                g.setColor(Color.BLACK);
                g.fillOval(x + 17, y + 12, 2, 2);
                g.fillOval(x + 22, y + 12, 2, 2);

                // Hat
                g.fillRect(x + 13, y + 7, 14, 3);
                g.fillRect(x + 17, y + 3, 6, 5);

                // Nose
                g.setColor(Color.ORANGE);
                g.fillRect(x + 20, y + 16, 5, 2);
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
                tileAppearances.get(block.getTileType()).draw(g, x, y, TILE_SIZE);
                g.setColor(GRID_COLOR);
                g.drawRect(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            }
        }

        drawDecorations(g);
        drawBlastPreview(g, frameServerBombs);

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

        Set<String> onMap = new HashSet<>();

        // Draw NPCs (under the players)
        for (NPC npc : frameNpcs) {
            if (!npc.alive) continue;
            String key = "n" + npc.id;
            onMap.add(key);
            sprites.drawNpc(g, key, npc.getX(), npc.getY(), TILE_SIZE, npc.color);
        }

        // Draw remote players, then the local one on top
        List<RemotePlayer> frameRemotePlayers = new ArrayList<>(remotePlayers.values());
        for (RemotePlayer rp : frameRemotePlayers) {
            String key = "p" + rp.id;
            onMap.add(key);
            sprites.drawPlayer(g, key, rp.x, rp.y, TILE_SIZE, rp.color, rp.alive);
            drawNameTag(g, labelFor(rp.name, rp.id), rp.x, rp.y);
        }
        onMap.add("local");
        sprites.drawPlayer(g, "local", player.getX(), player.getY(), TILE_SIZE, player.color, player.alive);
        drawNameTag(g, labelFor(player.name, localPlayerId), player.getX(), player.getY());
        sprites.retain(onMap);

        if (lobby) {
            drawLobbyOverlay(g);
        }

        // if dead, draw retry overlay
        if (!player.alive && !lobby) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setColor(new Color(0, 0, 0, 160));
            g2d.fillRect(0, 0, getWidth(), getHeight());
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("SansSerif", Font.BOLD, 28));
            String msg = "You Died";
            String hint = isLocalHost() ? "Press R to restart the round" : "Spectating - waiting for the host";
            int mw = g2d.getFontMetrics().stringWidth(msg);
            int hw = g2d.getFontMetrics().stringWidth(hint);
            g2d.drawString(msg, (getWidth() - mw) / 2, getHeight() / 2 - 10);
            g2d.drawString(hint, (getWidth() - hw) / 2, getHeight() / 2 + 30);
            g2d.dispose();
        }

        // notice banner (e.g. "Only the host can restart the round")
        String currentNotice = notice;
        if (currentNotice != null && System.currentTimeMillis() < noticeUntil) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setFont(new Font("SansSerif", Font.BOLD, 14));
            int w = g2d.getFontMetrics().stringWidth(currentNotice) + 24;
            int x = (getWidth() - w) / 2;
            g2d.setColor(new Color(0, 0, 0, 190));
            g2d.fillRoundRect(x, 8, w, 28, 12, 12);
            g2d.setColor(new Color(255, 220, 90));
            g2d.drawString(currentNotice, x + 12, 27);
            g2d.dispose();
        }

        if (menu != null) {
            menu.paint(g, getWidth(), getHeight());
        }
    }

    /** Small name label centred above a character. */
    private void drawNameTag(Graphics g, String label, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING,
                java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setFont(new Font("SansSerif", Font.BOLD, 10));
        int w = g2.getFontMetrics().stringWidth(label);
        int tx = x + (TILE_SIZE - w) / 2;
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRoundRect(tx - 3, y - 12, w + 6, 12, 6, 6);
        g2.setColor(Color.WHITE);
        g2.drawString(label, tx, y - 3);
        g2.dispose();
    }

    /**
     * Tints every tile a live bomb will hit, stronger as its fuse runs out, so
     * it is obvious where it is safe to stand. Follows the server's blast rule:
     * hard walls stop it, a soft block is hit and stops it.
     */
    private void drawBlastPreview(Graphics g, List<int[]> bombsToPreview) {
        TileType[][] m = map;
        Graphics2D g2 = (Graphics2D) g.create();
        int[][] dirs = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        for (int[] bomb : bombsToPreview) {
            if (bomb.length < 4) continue;
            int bx = bomb[0], by = bomb[1], radius = bomb[2], remainingMs = bomb[3];
            float urgency = 1f - Math.max(0, Math.min(remainingMs, FUSE_MS)) / (float) FUSE_MS;
            g2.setColor(new Color(255, 40, 20, (int) (45 + urgency * 110)));
            g2.fillRect(bx * TILE_SIZE, by * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            for (int[] d : dirs) {
                for (int step = 1; step <= radius; step++) {
                    int tx = bx + d[0] * step, ty = by + d[1] * step;
                    if (ty < 0 || tx < 0 || ty >= m.length || tx >= m[0].length) break;
                    if (m[ty][tx] == TileType.HARD_WALL) break;
                    g2.fillRect(tx * TILE_SIZE, ty * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                    if (m[ty][tx] == TileType.SOFT_BLOCK) break;
                }
            }
        }
        g2.dispose();
    }

    private void drawLobbyOverlay(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRect(0, 0, getWidth(), getHeight());

        int cx = getWidth() / 2;
        int y = getHeight() / 2 - 90;
        int panelW = 280, panelTop = y - 44;
        int panelH = Math.max(y + 34 + lobbyPlayers.size() * 24 + 44, getHeight() / 2 + 70) + 26 - panelTop;
        g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING, java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setColor(new Color(24, 26, 32, 235));
        g2.fillRoundRect(cx - panelW / 2, panelTop, panelW, panelH, 18, 18);
        g2.setColor(new Color(255, 205, 70));
        g2.setStroke(new java.awt.BasicStroke(2f));
        g2.drawRoundRect(cx - panelW / 2, panelTop, panelW, panelH, 18, 18);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 30));
        centered(g2, "LOBBY", cx, y);

        g2.setFont(new Font("SansSerif", Font.PLAIN, 14));
        List<String> names = lobbyPlayers;
        centered(g2, "Players (" + names.size() + "/4)", cx, y += 34);
        g2.setFont(new Font("SansSerif", Font.BOLD, 16));
        for (String name : names) {
            centered(g2, name, cx, y += 24);
        }

        g2.setFont(new Font("SansSerif", Font.BOLD, 18));
        g2.setColor(new Color(255, 220, 90));
        centered(g2, isLocalHost() ? "Press ENTER to start" : "Waiting for the host to start...",
                cx, Math.max(y + 44, getHeight() / 2 + 70));
        g2.dispose();
    }

    private static void centered(Graphics2D g2, String text, int cx, int y) {
        g2.drawString(text, cx - g2.getFontMetrics().stringWidth(text) / 2, y);
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
