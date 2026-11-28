package com.example.client;

import java.awt.Color;

public class DesertWall extends Wall {
    public DesertWall(int tileX, int tileY, int tileSize) {
        super(tileX, tileY, tileSize);
    }

    @Override
    public Color color() {
        return new Color(150, 95, 60);
    }
}
