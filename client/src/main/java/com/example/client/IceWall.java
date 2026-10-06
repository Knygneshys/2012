package com.example.client;

import java.awt.Color;

public class IceWall extends Wall {

    public IceWall(int tileX, int tileY, int tileSize) {
        super(tileX, tileY, tileSize);
    }

    @Override
    public Color color() {
        return new Color(180, 220, 255);
    }
}