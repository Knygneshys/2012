package com.example.client;

import javax.swing.JPanel;
import java.awt.*;

/**
 * Slim bar above the map: who you are, and your bombs, blast range and speed.
 * Filled by {@link com.example.client.observer.HudStateObserver}.
 */
public class HudBar extends JPanel {

    public static final int HEIGHT = 30;
    private static final Color BG = new Color(28, 30, 36);
    private static final Color TEXT = new Color(230, 232, 238);
    private static final Color MUTED = new Color(130, 135, 148);
    /** Server base speed; anything above it came from Speed powerups. */
    private static final int BASE_SPEED = 10;
    private static final int SPEED_STEP = 2;

    private volatile ClientGameState.PlayerSnapshot me;
    private volatile boolean host;

    public HudBar(int width) {
        setPreferredSize(new Dimension(width, HEIGHT));
        setBackground(BG);
    }

    public void update(ClientGameState.PlayerSnapshot me, boolean host, boolean lobby) {
        this.me = me;
        this.host = host;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int mid = HEIGHT / 2;
        g2.setFont(new Font("SansSerif", Font.BOLD, 13));
        FontMetrics fm = g2.getFontMetrics();
        int baseline = mid + fm.getAscent() / 2 - 2;

        ClientGameState.PlayerSnapshot p = me;
        if (p == null) {
            g2.setColor(MUTED);
            g2.drawString("Connecting...", 10, baseline);
            g2.dispose();
            return;
        }

        // left: colour dot + name
        int x = 10;
        g2.setColor(p.alive() ? p.color() : Character.DEAD_COLOR);
        g2.fillOval(x, mid - 6, 12, 12);
        x += 18;
        g2.setColor(TEXT);
        String name = (host ? "★ " : "") + p.name();
        g2.drawString(name, x, baseline);

        // right: three compact stats
        int available = Math.max(0, p.maxBombs() - p.activeBombs());
        int speedLevel = 1 + Math.max(0, (p.moveSpeed() - BASE_SPEED) / SPEED_STEP);
        String bombs = available + "/" + p.maxBombs();
        String fire = String.valueOf(p.bombRadius());
        String speed = String.valueOf(speedLevel);

        int right = getWidth() - 10;
        right = stat(g2, right, baseline, mid, speed, new Color(90, 200, 255), Icon.SPEED);
        right = stat(g2, right, baseline, mid, fire, new Color(255, 120, 60), Icon.FIRE);
        stat(g2, right, baseline, mid, bombs, TEXT, Icon.BOMB);
        g2.dispose();
    }

    private enum Icon { BOMB, FIRE, SPEED }

    /** Draws "icon value" ending at {@code right}; returns where the next stat should end. */
    private int stat(Graphics2D g2, int right, int baseline, int mid, String value, Color color, Icon icon) {
        int w = g2.getFontMetrics().stringWidth(value);
        int textX = right - w;
        g2.setColor(color);
        g2.drawString(value, textX, baseline);
        int ix = textX - 18;
        switch (icon) {
            case BOMB -> {
                g2.setColor(Color.BLACK);
                g2.fillOval(ix, mid - 6, 12, 12);
                g2.setColor(new Color(255, 150, 50));
                g2.fillRect(ix + 8, mid - 9, 3, 4);
            }
            case FIRE -> {
                g2.setColor(new Color(255, 90, 40));
                g2.fillPolygon(new int[]{ix + 6, ix + 12, ix + 6, ix}, new int[]{mid - 8, mid + 3, mid + 7, mid + 3}, 4);
                g2.setColor(new Color(255, 210, 80));
                g2.fillOval(ix + 3, mid - 1, 6, 7);
            }
            case SPEED -> {
                g2.setColor(new Color(90, 200, 255));
                g2.fillPolygon(new int[]{ix + 7, ix + 1, ix + 6, ix + 4, ix + 11, ix + 6},
                        new int[]{mid - 8, mid + 1, mid + 1, mid + 8, mid - 1, mid - 1}, 6);
            }
        }
        return ix - 16;
    }
}
