package com.example;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.JPanel;

public class MapPanel extends JPanel {
    private static final int TILE_SIZE = 40;
    private static final Color GRID_COLOR = new Color(30, 30, 30);
    private static final Color FLOOR_COLOR = new Color(205, 190, 160);
    private static final Color HARD_WALL_COLOR = new Color(70, 90, 120);
    private static final Color SOFT_BLOCK_COLOR = new Color(160, 110, 70);

    private final TileType[][] map;

    public MapPanel(TileType[][] map) {
        this.map = map;
        setPreferredSize(new Dimension(
                DemoMapFactory.MAP_WIDTH * TILE_SIZE,
                DemoMapFactory.MAP_HEIGHT * TILE_SIZE
        ));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (int y = 0; y < DemoMapFactory.MAP_HEIGHT; y++) {
            for (int x = 0; x < DemoMapFactory.MAP_WIDTH; x++) {
                g.setColor(tileColor(map[y][x]));
                g.fillRect(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                g.setColor(GRID_COLOR);
                g.drawRect(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            }
        }
    }

    private Color tileColor(TileType tile) {
        return switch (tile) {
            case FLOOR -> FLOOR_COLOR;
            case HARD_WALL -> HARD_WALL_COLOR;
            case SOFT_BLOCK -> SOFT_BLOCK_COLOR;
        };
    }
}
