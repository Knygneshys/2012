package com.example.client;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * The first screen, shown inside the game window: title, server address,
 * player name and a Connect button. Errors appear on the screen itself
 * instead of in popup dialogs.
 */
public class TitleScreen extends JPanel {

    /** Called with (host, port, name) when the player asks to connect. */
    public interface ConnectHandler {
        void connect(String host, int port, String name);
    }

    private static final Color BG_TOP = new Color(34, 40, 58);
    private static final Color BG_BOTTOM = new Color(16, 18, 26);
    private static final Color GOLD = new Color(255, 205, 70);
    private static final Color TEXT = new Color(235, 237, 242);
    private static final Color FIELD_BG = new Color(44, 48, 60);
    private static final Color ERROR = new Color(255, 110, 100);

    private final JTextField hostField;
    private final JTextField portField;
    private final JTextField nameField;
    private final JButton connectButton;
    private final JLabel status;
    private final CharacterSprite sprites = new CharacterSprite();
    private final Timer animation;

    public TitleScreen(String defaultHost, int defaultPort, Dimension size, ConnectHandler handler) {
        setPreferredSize(size);
        setLayout(new GridBagLayout());

        hostField = field(new JTextField(defaultHost, 14));
        portField = field(new JTextField(String.valueOf(defaultPort), 14));
        nameField = field(new JTextField("Player", 14));

        connectButton = new JButton("CONNECT");
        connectButton.setFont(new Font("SansSerif", Font.BOLD, 15));
        connectButton.setForeground(Color.BLACK);
        connectButton.setBackground(GOLD);
        connectButton.setOpaque(true);
        connectButton.setBorderPainted(false);
        connectButton.setFocusPainted(false);
        connectButton.setBorder(new EmptyBorder(8, 20, 8, 20));
        connectButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        status = new JLabel(" ", SwingConstants.CENTER);
        status.setFont(new Font("SansSerif", Font.BOLD, 12));

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 6, 5, 6);
        c.fill = GridBagConstraints.HORIZONTAL;
        addRow(form, c, 0, "Server", hostField);
        addRow(form, c, 1, "Port", portField);
        addRow(form, c, 2, "Name", nameField);
        c.gridx = 0; c.gridy = 3; c.gridwidth = 2;
        c.insets = new Insets(14, 6, 4, 6);
        form.add(connectButton, c);
        c.gridy = 4; c.insets = new Insets(4, 6, 4, 6);
        form.add(status, c);

        GridBagConstraints outer = new GridBagConstraints();
        outer.insets = new Insets(120, 0, 0, 0); // leave room for the painted title
        add(form, outer);

        Runnable submit = () -> {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                showError("Please enter a player name");
                return;
            }
            int port;
            try {
                port = Integer.parseInt(portField.getText().trim());
                if (port < 1 || port > 65535) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                showError("Port must be a number between 1 and 65535");
                return;
            }
            setBusy(true);
            handler.connect(hostField.getText().trim(), port, name);
        };
        connectButton.addActionListener(e -> submit.run());
        nameField.addActionListener(e -> submit.run());
        hostField.addActionListener(e -> submit.run());
        portField.addActionListener(e -> submit.run());

        // keeps the little characters on the title walking
        animation = new Timer(60, e -> repaint());
        animation.start();
    }

    private JTextField field(JTextField f) {
        f.setFont(new Font("SansSerif", Font.PLAIN, 14));
        f.setBackground(FIELD_BG);
        f.setForeground(TEXT);
        f.setCaretColor(TEXT);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(FIELD_BG.brighter()), new EmptyBorder(5, 8, 5, 8)));
        return f;
    }

    private void addRow(JPanel form, GridBagConstraints c, int row, String label, JComponent input) {
        JLabel l = new JLabel(label);
        l.setForeground(new Color(170, 175, 190));
        l.setFont(new Font("SansSerif", Font.BOLD, 13));
        c.gridx = 0; c.gridy = row; c.gridwidth = 1; c.weightx = 0;
        form.add(l, c);
        c.gridx = 1; c.weightx = 1;
        form.add(input, c);
    }

    /** Re-enables the form and shows why connecting failed. */
    public void showError(String message) {
        setBusy(false);
        status.setForeground(ERROR);
        status.setText(message);
    }

    public void setBusy(boolean busy) {
        connectButton.setEnabled(!busy);
        connectButton.setText(busy ? "CONNECTING..." : "CONNECT");
        if (busy) {
            status.setForeground(TEXT);
            status.setText(" ");
        }
    }

    /** Called when leaving the title screen for the game. */
    public void stop() {
        animation.stop();
    }

    public void start() {
        setBusy(false);
        animation.start();
        SwingUtilities.invokeLater(nameField::requestFocusInWindow);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setPaint(new GradientPaint(0, 0, BG_TOP, 0, getHeight(), BG_BOTTOM));
        g2.fillRect(0, 0, getWidth(), getHeight());

        // faint floor grid
        g2.setColor(new Color(255, 255, 255, 12));
        for (int x = 0; x < getWidth(); x += MapPanel.TILE_SIZE) g2.drawLine(x, 0, x, getHeight());
        for (int y = 0; y < getHeight(); y += MapPanel.TILE_SIZE) g2.drawLine(0, y, getWidth(), y);

        // title
        int cx = getWidth() / 2;
        g2.setFont(new Font("SansSerif", Font.BOLD, 44));
        String title = "BOMBERMAN";
        int tw = g2.getFontMetrics().stringWidth(title);
        g2.setColor(new Color(0, 0, 0, 120));
        g2.drawString(title, cx - tw / 2 + 3, 83);
        g2.setColor(GOLD);
        g2.drawString(title, cx - tw / 2, 80);
        g2.setFont(new Font("SansSerif", Font.BOLD, 14));
        g2.setColor(TEXT);
        String sub = "MULTIPLAYER  ·  2-4 PLAYERS";
        g2.drawString(sub, cx - g2.getFontMetrics().stringWidth(sub) / 2, 106);

        // a few characters walking along the bottom
        int t = (int) ((System.currentTimeMillis() / 25) % (getWidth() + 200));
        int baseY = getHeight() - 60;
        sprites.drawPlayer(g2, "t1", t - 60, baseY, MapPanel.TILE_SIZE, new Color(0x3b82f6), true);
        sprites.drawPlayer(g2, "t2", t - 110, baseY, MapPanel.TILE_SIZE, new Color(0xef4444), true);
        sprites.drawNpc(g2, "t3", t - 190, baseY, MapPanel.TILE_SIZE, new Color(0xff8800));
        g2.dispose();
    }
}
