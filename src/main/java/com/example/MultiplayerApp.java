package com.example;

import com.example.client.NetworkClient;
import com.example.network.ServerResponseMessage;

import javax.swing.*;
import java.awt.*;

/**
 * Multiplayer Bomberman Client - connects to a server.
 */
public class MultiplayerApp {
    private static NetworkClient networkClient;
    private static JFrame connectionFrame;

    public static void main(String[] args) {
        String host = "localhost";
        int port = 9876;

        if (args.length >= 1) {
            host = args[0];
        }
        if (args.length >= 2) {
            try {
                port = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port: " + args[1]);
                System.exit(1);
            }
        }

        final String finalHost = host;
        final int finalPort = port;
        SwingUtilities.invokeLater(() -> showConnectionDialog(finalHost, finalPort));
    }

    private static void showConnectionDialog(String defaultHost, int defaultPort) {
        connectionFrame = new JFrame("Bomberman - Connect to Server");
        connectionFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        connectionFrame.setResizable(false);

        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel hostLabel = new JLabel("Server Host:");
        JTextField hostField = new JTextField(defaultHost);

        JLabel portLabel = new JLabel("Server Port:");
        JSpinner portSpinner = new JSpinner(new SpinnerNumberModel(defaultPort, 1024, 65535, 1));

        JLabel nameLabel = new JLabel("Player Name:");
        JTextField nameField = new JTextField("Player");

        JButton connectButton = new JButton("Connect");

        panel.add(hostLabel);
        panel.add(hostField);
        panel.add(portLabel);
        panel.add(portSpinner);
        panel.add(nameLabel);
        panel.add(nameField);
        panel.add(new JLabel());
        panel.add(connectButton);

        connectButton.addActionListener(e -> {
            String host = hostField.getText().trim();
            int port = (Integer) portSpinner.getValue();
            String playerName = nameField.getText().trim();

            if (playerName.isEmpty()) {
                JOptionPane.showMessageDialog(connectionFrame, "Player name cannot be empty!");
                return;
            }

            connectButton.setEnabled(false);
            connectButton.setText("Connecting...");

            new Thread(() -> {
                try {
                    networkClient = new NetworkClient(host, port);
                    if (networkClient.connect()) {
                        System.out.println("✅ Connected to server!");

                        // Set up response handler
                        networkClient.setOnServerResponse(msg -> {
                            if (msg.success) {
                                connectionFrame.dispose();
                                launchGame(playerName);
                            } else {
                                JOptionPane.showMessageDialog(connectionFrame,
                                        "Failed to join: " + msg.message, "Connection Error",
                                        JOptionPane.ERROR_MESSAGE);
                                connectButton.setEnabled(true);
                                connectButton.setText("Connect");
                                networkClient.disconnect();
                            }
                        });

                        networkClient.joinGame(playerName);
                    } else {
                        JOptionPane.showMessageDialog(connectionFrame,
                                "Failed to connect to server at " + host + ":" + port,
                                "Connection Error", JOptionPane.ERROR_MESSAGE);
                        connectButton.setEnabled(true);
                        connectButton.setText("Connect");
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(connectionFrame,
                            "Error: " + ex.getMessage(), "Connection Error",
                            JOptionPane.ERROR_MESSAGE);
                    connectButton.setEnabled(true);
                    connectButton.setText("Connect");
                }
            }).start();
        });

        connectionFrame.add(panel);
        connectionFrame.pack();
        connectionFrame.setLocationRelativeTo(null);
        connectionFrame.setVisible(true);
    }

    private static void launchGame(String playerName) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Bomberman - Multiplayer");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    if (networkClient != null) {
                        networkClient.disconnect();
                    }
                }
            });

            TileType[][] map = DemoMapFactory.createDefaultMap();
            Player player = new Player(1 * MapPanel.TILE_SIZE, 1 * MapPanel.TILE_SIZE,
                    10, playerName, Color.BLUE, MapPanel.TILE_SIZE, MapPanel.TILE_SIZE);

            MapPanel mapPanel = new MapPanel(map, player);
            GameController controller = new GameController(player, mapPanel, map, networkClient);

            frame.add(mapPanel);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setResizable(false);
            frame.setVisible(true);
            mapPanel.setFocusable(true);
            mapPanel.requestFocusInWindow();

            System.out.println("🎮 Game started! Use WASD to move, SPACE to place bomb, R to reset.");
            System.out.println("👤 You are: " + playerName);
        });
    }
}

