package com.example.client;

import java.awt.Color;

/**
 * An indestructible wall. Bombs cannot destroy it and a blast stops as soon as
 * it reaches one.
 */
public class Wall extends Block {
    private static final Color WALL_COLOR = new Color(70, 90, 120);

    public Wall(int tileX, int tileY, int tileSize) {
        super(tileX, tileY, TileType.HARD_WALL, tileSize);
    }

    @Override
    public boolean isPassable() {
        return false;
    }

    @Override
    public boolean isDestructible() {
        return false;
    }

    @Override
    public Color color() {
        return WALL_COLOR;
    }
}