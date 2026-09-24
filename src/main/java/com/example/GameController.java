package com.example;

import java.awt.event.KeyEvent;

public class GameController {
    private final Player player;
    private final MapPanel mapPanel;
    private final TileType[][] map;
    public GameController(Player player, MapPanel mapPanel, TileType[][] map) {
        this.player = player;
        this.mapPanel = mapPanel;
        this.map = map;
        mapPanel.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                int key = e.getKeyCode();
                switch (key) {
                    case KeyEvent.VK_W -> player.move(0, -player.moveSpeed, map);
                    case KeyEvent.VK_S -> player.move(0, player.moveSpeed, map);
                    case KeyEvent.VK_A -> player.move(-player.moveSpeed, 0, map);
                    case KeyEvent.VK_D -> player.move(player.moveSpeed, 0, map);
                    case KeyEvent.VK_SPACE -> mapPanel.placeBomb(player);
                }
                mapPanel.repaint();
            }
        });
    }
    
}
