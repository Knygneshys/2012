package com.example;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.awt.Color;
public class App {
    public static void main(String[] args) {
        Player player = new Player(1*MapPanel.TILE_SIZE, 1*MapPanel.TILE_SIZE, 10, "Player1", Color.BLUE, MapPanel.TILE_SIZE, MapPanel.TILE_SIZE);
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Bomberman Demo");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            TileType[][] map = DemoMapFactory.createDefaultMap();
            MapPanel mapPanel = new MapPanel(map, player);
            GameController controller = new GameController(player, mapPanel,map);
            frame.add(mapPanel);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setResizable(false);
            frame.setVisible(true);
            mapPanel.setFocusable(true);
            mapPanel.requestFocusInWindow();
        });
    }
}