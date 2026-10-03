package com.example.client;

import java.awt.Color;

/**
 * Empty walkable ground. This is what a destroyed {@link BreakableWall} turns
 * into, and it is the only block characters can stand on.
 */
public class Passage extends Block {
    private static final Color PASSAGE_COLOR = new Color(205, 190, 160);

    public Passage(int tileX, int tileY, int tileSize) {
        super(tileX, tileY, TileType.FLOOR, tileSize);
    }

    @Override
    public boolean isPassable() {
        return true;
    }

    @Override
    public boolean isDestructible() {
        return false;
    }

    @Override
    public Color color() {
        return PASSAGE_COLOR;
    }
}