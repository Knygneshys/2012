package com.example.client;

import com.example.client.command.Command;
import com.example.client.network.NetworkClient;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.IntConsumer;
import java.util.function.Supplier;

/**
 * The Esc menu: a small list drawn over the map. W/S or arrows to choose,
 * Enter to pick, A/D or left/right to change a setting, Esc to close. The
 * mouse works too.
 * <p>
 * Host-only entries are simply not shown to other players. That is only
 * tidiness: the server's protection proxy is what actually refuses them.
 */
public class InGameMenu {

    private static final Color PANEL = new Color(18, 20, 26, 225);
    private static final Color TEXT = new Color(225, 228, 235);
    private static final Color DIM = new Color(140, 145, 158);
    private static final Color ACCENT = new Color(255, 205, 70);
    private static final Font ITEM_FONT = new Font("SansSerif", Font.BOLD, 15);
    private static final int ITEM_H = 32;
    private static final int WIDTH = 210;
    private static final int PAD = 14;

    /**
     * One row. A setting row has a {@code change} action (called with -1 / +1)
     * and stays open when used; a plain row runs {@code action} and closes.
     */
    private record Item(Supplier<String> label, BooleanSupplier visible, Runnable action, IntConsumer change) {
        boolean isSetting() {
            return change != null;
        }
    }

    private final List<Item> items = new ArrayList<>();
    private boolean open;
    private int selected;
    private final List<Rectangle> hitBoxes = new ArrayList<>();

    public InGameMenu(NetworkClient networkClient, BooleanSupplier inLobby,
                      Supplier<String> themeLabel, IntConsumer changeTheme,
                      Command startCommand, Command restartCommand, Command lobbyCommand,
                      Runnable leaveAction) {
        BooleanSupplier always = () -> true;
        BooleanSupplier hostInLobby = () -> networkClient.isHost() && inLobby.getAsBoolean();
        BooleanSupplier hostPlaying = () -> networkClient.isHost() && !inLobby.getAsBoolean();

        items.add(new Item(() -> "Resume", always, this::close, null));
        items.add(new Item(() -> "Start game", hostInLobby, startCommand::execute, null));
        items.add(new Item(() -> "Restart round", hostPlaying, restartCommand::execute, null));
        items.add(new Item(() -> "Back to lobby", hostPlaying, lobbyCommand::execute, null));
        items.add(new Item(() -> "Theme: " + themeLabel.get(), always, null, changeTheme));
        items.add(new Item(() -> "Leave game", always, leaveAction, null));
    }

    public boolean isOpen() {
        return open;
    }

    public void open() {
        open = true;
        selected = 0;
    }

    public void close() {
        open = false;
    }

    public void handleKey(int key) {
        List<Item> visible = visibleItems();
        if (visible.isEmpty()) return;
        selected = Math.min(selected, visible.size() - 1);
        Item current = visible.get(selected);
        switch (key) {
            case KeyEvent.VK_ESCAPE -> close();
            case KeyEvent.VK_UP, KeyEvent.VK_W -> selected = Math.floorMod(selected - 1, visible.size());
            case KeyEvent.VK_DOWN, KeyEvent.VK_S -> selected = Math.floorMod(selected + 1, visible.size());
            case KeyEvent.VK_LEFT, KeyEvent.VK_A -> { if (current.isSetting()) current.change().accept(-1); }
            case KeyEvent.VK_RIGHT, KeyEvent.VK_D -> { if (current.isSetting()) current.change().accept(1); }
            case KeyEvent.VK_ENTER, KeyEvent.VK_SPACE -> activate(current);
            default -> { }
        }
    }

    public void handleMouseMove(Point p) {
        int i = hit(p);
        if (i >= 0) selected = i;
    }

    public void handleClick(Point p) {
        int i = hit(p);
        List<Item> visible = visibleItems();
        if (i >= 0 && i < visible.size()) activate(visible.get(i));
    }

    private void activate(Item item) {
        if (item.isSetting()) {
            item.change().accept(1);   // Enter / click steps a setting forward
            return;
        }
        close();
        item.action().run();
    }

    private int hit(Point p) {
        for (int i = 0; i < hitBoxes.size(); i++) {
            if (hitBoxes.get(i).contains(p)) return i;
        }
        return -1;
    }

    private List<Item> visibleItems() {
        List<Item> visible = new ArrayList<>();
        for (Item item : items) {
            if (item.visible().getAsBoolean()) visible.add(item);
        }
        return visible;
    }

    public void paint(Graphics g, int width, int height) {
        if (!open) return;
        List<Item> visible = visibleItems();
        selected = Math.min(selected, Math.max(0, visible.size() - 1));

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g2.setColor(new Color(0, 0, 0, 110));
        g2.fillRect(0, 0, width, height);

        int panelH = PAD * 2 + visible.size() * ITEM_H;
        int px = (width - WIDTH) / 2;
        int py = (height - panelH) / 2;
        g2.setColor(PANEL);
        g2.fillRoundRect(px, py, WIDTH, panelH, 14, 14);

        g2.setFont(ITEM_FONT);
        FontMetrics fm = g2.getFontMetrics();
        hitBoxes.clear();
        int y = py + PAD;
        for (int i = 0; i < visible.size(); i++) {
            Item item = visible.get(i);
            boolean sel = i == selected;
            hitBoxes.add(new Rectangle(px, y, WIDTH, ITEM_H));
            int baseline = y + (ITEM_H + fm.getAscent() - fm.getDescent()) / 2;

            String label = item.label().get();
            if (item.isSetting() && sel) label = "‹  " + label + "  ›";
            g2.setColor(sel ? ACCENT : (item.isSetting() ? DIM : TEXT));
            g2.drawString(label, px + (WIDTH - fm.stringWidth(label)) / 2, baseline);
            y += ITEM_H;
        }
        g2.dispose();
    }
}
