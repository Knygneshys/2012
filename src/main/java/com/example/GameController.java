package com.example;

import java.awt.event.KeyEvent;

public class GameController {
    private final Player player;
    private final MapPanel mapPanel;

    public GameController(Player player, MapPanel mapPanel, TileType[][] map) {
        this.player = player;
        this.mapPanel = mapPanel;
        mapPanel.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                int key = e.getKeyCode();
                switch (key) {
                    case KeyEvent.VK_W -> player.move(0, -player.moveSpeed, mapPanel.getMap());
                    case KeyEvent.VK_S -> player.move(0, player.moveSpeed, mapPanel.getMap());
                    case KeyEvent.VK_A -> player.move(-player.moveSpeed, 0, mapPanel.getMap());
                    case KeyEvent.VK_D -> player.move(player.moveSpeed, 0, mapPanel.getMap());
                    case KeyEvent.VK_SPACE -> mapPanel.placeBomb(player);
                    case KeyEvent.VK_R -> mapPanel.resetGame();
                }
                mapPanel.repaint();
            }
        });
    }
    
}
