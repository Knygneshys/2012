package com.example.client;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.util.HashMap;
import java.util.Map;

/**
 * Draws the little characters instead of plain circles: a player (round head,
 * body in the player's colour, walking feet, eyes looking where it is going)
 * and an NPC slime that wobbles as it moves.
 * <p>
 * The server only sends positions, so the facing direction and walk cycle are
 * worked out here from how each character's position changed between frames.
 */
public final class CharacterSprite {

    public enum Facing { UP, DOWN, LEFT, RIGHT }

    private static final Color SKIN = new Color(255, 222, 186);
    private static final Color OUTLINE = new Color(25, 25, 30);
    /** How long after the last position change a character still counts as walking. */
    private static final long WALK_GRACE_MS = 120;

    /** Per-character animation state, keyed by e.g. "p3" or "n1". */
    private final Map<String, Motion> motions = new HashMap<>();

    private static final class Motion {
        int lastX, lastY;
        Facing facing = Facing.DOWN;
        int walked;      // pixels walked, drives the step cycle
        long lastMoved;
    }

    private Motion track(String key, int x, int y) {
        Motion m = motions.get(key);
        long now = System.currentTimeMillis();
        if (m == null) {
            m = new Motion();
            m.lastX = x;
            m.lastY = y;
            motions.put(key, m);
            return m;
        }
        int dx = x - m.lastX, dy = y - m.lastY;
        if (dx != 0 || dy != 0) {
            // big jumps (respawn, reset) are teleports, not steps
            if (Math.abs(dx) + Math.abs(dy) < MapPanel.TILE_SIZE) {
                m.facing = Math.abs(dx) >= Math.abs(dy)
                        ? (dx > 0 ? Facing.RIGHT : Facing.LEFT)
                        : (dy > 0 ? Facing.DOWN : Facing.UP);
                m.walked += Math.abs(dx) + Math.abs(dy);
                m.lastMoved = now;
            }
            m.lastX = x;
            m.lastY = y;
        }
        return m;
    }

    /** Draws a player whose top-left tile corner is (x, y). */
    public void drawPlayer(Graphics g, String key, int x, int y, int size, Color color, boolean alive) {
        Motion m = track(key, x, y);
        boolean walking = alive && System.currentTimeMillis() - m.lastMoved < WALK_GRACE_MS;
        int step = walking ? ((m.walked / 8) % 2 == 0 ? 1 : -1) : 0;
        Color body = alive ? color : Character.DEAD_COLOR;
        Color skin = alive ? SKIN : new Color(200, 200, 200);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.translate(x, y);
        float s = size / 40f; // drawn on a 40px grid, scaled to the tile
        g2.scale(s, s);
        g2.setStroke(new BasicStroke(1.5f));

        // shadow
        g2.setColor(new Color(0, 0, 0, 60));
        g2.fill(new Ellipse2D.Float(9, 34, 22, 5));

        // feet (alternate while walking)
        g2.setColor(OUTLINE);
        g2.fill(new RoundRectangle2D.Float(12, 32 + Math.min(0, step) * 2, 7, 5, 3, 3));
        g2.fill(new RoundRectangle2D.Float(21, 32 + Math.min(0, -step) * 2, 7, 5, 3, 3));

        // arms
        int armSwing = step * 2;
        g2.setColor(skin);
        g2.fill(new Ellipse2D.Float(7, 22 + armSwing, 6, 6));
        g2.fill(new Ellipse2D.Float(27, 22 - armSwing, 6, 6));

        // body
        RoundRectangle2D bodyShape = new RoundRectangle2D.Float(11, 19, 18, 15, 8, 8);
        g2.setColor(body);
        g2.fill(bodyShape);
        g2.setColor(OUTLINE);
        g2.draw(bodyShape);
        // belt
        g2.setColor(body.darker());
        g2.fillRect(12, 28, 16, 2);

        // head (bobs a little while walking)
        int bob = walking && step > 0 ? -1 : 0;
        Ellipse2D head = new Ellipse2D.Float(10, 2 + bob, 20, 19);
        g2.setColor(skin);
        g2.fill(head);
        g2.setColor(OUTLINE);
        g2.draw(head);
        // headband in the player's colour, so players are easy to tell apart
        g2.setColor(body);
        g2.fillRect(11, 5 + bob, 18, 3);

        // face
        if (!alive) {
            g2.setColor(OUTLINE);
            drawX(g2, 15, 12 + bob);
            drawX(g2, 23, 12 + bob);
        } else if (m.facing != Facing.UP) {
            int look = switch (m.facing) {
                case LEFT -> -3;
                case RIGHT -> 3;
                default -> 0;
            };
            g2.setColor(OUTLINE);
            if (m.facing == Facing.DOWN) {
                g2.fill(new Ellipse2D.Float(14, 11 + bob, 3.5f, 4.5f));
                g2.fill(new Ellipse2D.Float(22.5f, 11 + bob, 3.5f, 4.5f));
                g2.drawArc(16, 14 + bob, 8, 4, 200, 140); // smile
            } else {
                // side view: both eyes shifted towards the facing side
                g2.fill(new Ellipse2D.Float(16 + look, 11 + bob, 3.5f, 4.5f));
                g2.fill(new Ellipse2D.Float(21.5f + look, 11 + bob, 3.5f, 4.5f));
            }
        }
        g2.dispose();
    }

    /** Draws an NPC slime whose top-left tile corner is (x, y). */
    public void drawNpc(Graphics g, String key, int x, int y, int size, Color color) {
        Motion m = track(key, x, y);
        long now = System.currentTimeMillis();
        // squash and stretch over time, faster while moving
        boolean moving = now - m.lastMoved < WALK_GRACE_MS;
        double phase = (now % 1000) / 1000.0 * Math.PI * 2 * (moving ? 2 : 1);
        float squash = (float) Math.sin(phase) * 1.5f;

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.translate(x, y);
        float s = size / 40f;
        g2.scale(s, s);

        g2.setColor(new Color(0, 0, 0, 60));
        g2.fill(new Ellipse2D.Float(7, 33, 26, 5));

        // blob: dome on top, wavy skirt at the bottom
        float top = 8 + squash;
        float w = 28 - squash, left = 20 - w / 2;
        Shape dome = new RoundRectangle2D.Float(left, top, w, 34 - top, 22, 22);
        g2.setColor(color);
        g2.fill(dome);
        g2.setColor(color.darker());
        for (int i = 0; i < 4; i++) {
            g2.fill(new Ellipse2D.Float(left + i * (w / 4f), 30, w / 4f, 5));
        }
        g2.setColor(OUTLINE);
        g2.setStroke(new BasicStroke(1.5f));
        g2.draw(dome);
        // shine
        g2.setColor(new Color(255, 255, 255, 110));
        g2.fill(new Ellipse2D.Float(left + 5, top + 4, 6, 4));

        // eyes look where it is going
        int lx = switch (m.facing) { case LEFT -> -2; case RIGHT -> 2; default -> 0; };
        int ly = switch (m.facing) { case UP -> -2; case DOWN -> 1; default -> 0; };
        g2.setColor(Color.WHITE);
        g2.fill(new Ellipse2D.Float(13, top + 8, 6, 7));
        g2.fill(new Ellipse2D.Float(21, top + 8, 6, 7));
        g2.setColor(OUTLINE);
        g2.fill(new Ellipse2D.Float(14.5f + lx, top + 10 + ly, 3, 3.5f));
        g2.fill(new Ellipse2D.Float(22.5f + lx, top + 10 + ly, 3, 3.5f));
        g2.dispose();
    }

    /** Forgets characters that are no longer on the map. */
    public void retain(java.util.Set<String> keys) {
        motions.keySet().retainAll(keys);
    }

    private static void drawX(Graphics2D g2, int cx, int cy) {
        g2.drawLine(cx - 2, cy - 2, cx + 2, cy + 2);
        g2.drawLine(cx - 2, cy + 2, cx + 2, cy - 2);
    }
}
