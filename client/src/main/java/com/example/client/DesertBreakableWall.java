package com.example.client;

import java.awt.Color;

public class DesertBreakableWall extends BreakableWall {
    public DesertBreakableWall(int tileX, int tileY, int tileSize) {
        super(tileX, tileY, tileSize);
    }

    @Override
    public Block destroyed() {
        return new DesertPassage(tileX, tileY, tileSize);
    }

    @Override
    public Color color() {
        return new Color(215, 160, 90);
    }
}
