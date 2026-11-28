package com.example.client;

import com.example.client.network.NetworkClient;

import javax.swing.*;
import java.awt.*;

/**
 * Multiplayer Bomberman Client - connects to a server over the network.
 * <p>
 * Everything happens in one window: the {@link TitleScreen} first, then the
 * game (HUD on top, map below). "Leave game" in the Esc menu comes back to the
 * title screen.
 */
public class MultiplayerApp {
    private static final GameLogger log = GameLogger.getInstance();

    private static final String TITLE_CARD = "title";
    private static final String GAME_CARD = "game";

    private static NetworkClient networkClient;
    private static JFrame frame;
    private static JPanel cards;
    private static TitleScreen titleScreen;
    private static JPanel gameView;

    public static void main(String[] args) {
        String host = "localhost";
        int port = 8080;

        if (args.length >= 1) {
            host = args[0];
        }
        if (args.length >= 2) {
            try {
                port = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                log.error("Invalid port: " + args[1], null);
                System.exit(1);
            }
        }

        final String finalHost = host;
        final int finalPort = port;
        SwingUtilities.invokeLater(() -> createWindow(finalHost, finalPort));
    }

    private static Dimension gameSize() {
        return new Dimension(DemoMapFactory.MAP_WIDTH * MapPanel.TILE_SIZE,
                DemoMapFactory.MAP_HEIGHT * MapPanel.TILE_SIZE + HudBar.HEIGHT);
    }

    private static void createWindow(String defaultHost, int defaultPort) {
        frame = new JFrame("Bomberman - Multiplayer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                if (networkClient != null) {
                    networkClient.disconnect();
                }
            }
        });

        cards = new JPanel(new CardLayout());
        titleScreen = new TitleScreen(defaultHost, defaultPort, gameSize(), MultiplayerApp::connect);
        cards.add(titleScreen, TITLE_CARD);

        frame.add(cards);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        titleScreen.start();
    }

    private static void connect(String host, int port, String playerName) {
        new Thread(() -> {
            try {
                NetworkClient client = new NetworkClient(host, port);
                if (!client.connect()) {
                    SwingUtilities.invokeLater(() ->
                            titleScreen.showError("Could not reach a server at " + host + ":" + port));
                    return;
                }
                log.info("Connected to server!");
                networkClient = client;

                // The join reply decides whether we enter the game
                java.util.concurrent.atomic.AtomicBoolean answered = new java.util.concurrent.atomic.AtomicBoolean();
                client.setOnServerResponse(msg -> SwingUtilities.invokeLater(() -> {
                    if (answered.getAndSet(true)) return; // only the first reply matters
                    if (msg.success) {
                        showGame(playerName);
                    } else {
                        titleScreen.showError("Could not join: " + msg.message);
                        client.disconnect();
                    }
                }));
                client.joinGame(playerName);
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> titleScreen.showError("Error: " + ex.getMessage()));
            }
        }, "connect").start();
    }

    private static void showGame(String playerName) {
        TileType[][] map = DemoMapFactory.createDefaultMap();
        Player player = new Player(1 * MapPanel.TILE_SIZE, 1 * MapPanel.TILE_SIZE,
                10, playerName, Color.BLUE, MapPanel.TILE_SIZE, MapPanel.TILE_SIZE);

        MapPanel mapPanel = new MapPanel(map, player);
        GameController controller = new GameController(player, mapPanel, map, networkClient);
        controller.setOnLeave(MultiplayerApp::showTitle);

        if (gameView != null) {
            cards.remove(gameView);
        }
        gameView = new JPanel(new BorderLayout());
        gameView.add(controller.getHudBar(), BorderLayout.NORTH);
        gameView.add(mapPanel, BorderLayout.CENTER);
        cards.add(gameView, GAME_CARD);

        titleScreen.stop();
        ((CardLayout) cards.getLayout()).show(cards, GAME_CARD);
        frame.pack();
        mapPanel.setFocusable(true);
        mapPanel.requestFocusInWindow();

        log.info("Game started! WASD/arrows to move, SPACE bomb, ENTER start (host), R restart (host), ESC menu.");
        log.info("You are: " + playerName);
    }

    private static void showTitle() {
        networkClient = null;
        ((CardLayout) cards.getLayout()).show(cards, TITLE_CARD);
        if (gameView != null) {
            cards.remove(gameView);
            gameView = null;
        }
        frame.pack();
        titleScreen.start();
    }
}
