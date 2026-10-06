package com.example.client;

import java.awt.Color;

public class IcePassage extends Passage {

    public IcePassage(int tileX, int tileY, int tileSize) {
        super(tileX, tileY, tileSize);
    }

    @Override
    public Color color() {
        return new Color(220, 240, 255);
    }
}