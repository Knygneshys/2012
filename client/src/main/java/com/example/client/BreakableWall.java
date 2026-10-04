package com.example.client;

import java.awt.Color;

/**
 * A destructible wall (soft block). It blocks movement until a bomb blast
 * destroys it, after which it leaves an empty {@link Passage} behind.
 */
public class BreakableWall extends Block {
    private static final Color BREAKABLE_WALL_COLOR = new Color(160, 110, 70);

    public BreakableWall(int tileX, int tileY, int tileSize) {
        super(tileX, tileY, TileType.SOFT_BLOCK, tileSize);
    }

    @Override
    public boolean isPassable() {
        return false;
    }

    @Override
    public boolean isDestructible() {
        return true;
    }

    @Override
    public Block destroyed() {
        return new Passage(tileX, tileY, tileSize);
    }

    @Override
    public Color color() {
        return BREAKABLE_WALL_COLOR;
    }
}