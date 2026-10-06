package com.example.client;

import java.awt.Color;

public class IceBreakableWall extends BreakableWall {

    public IceBreakableWall(int tileX, int tileY, int tileSize) {
        super(tileX, tileY, tileSize);
    }

    @Override
    public Block destroyed() {
        return new IcePassage(tileX, tileY, tileSize);
    }

    @Override
    public Color color() {
        return new Color(120, 180, 230);
    }
}