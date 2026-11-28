package com.example.client;

import java.awt.Color;

public class DesertPassage extends Passage {
    public DesertPassage(int tileX, int tileY, int tileSize) {
        super(tileX, tileY, tileSize);
    }

    @Override
    public Color color() {
        return new Color(245, 222, 170);
    }
}
