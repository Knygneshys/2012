package com.example.client.factories;

import com.example.client.Block;
import com.example.client.IceWall;
import com.example.client.IceBreakableWall;
import com.example.client.IcePassage;
import com.example.client.Decoration;
import com.example.client.DecorationStyle;
import java.awt.Color;

public class IceMapElementFactory implements MapElementFactory {

    @Override
    public Block createWall(int x, int y, int tileSize) {
        return new IceWall(x, y, tileSize);
    }

    @Override
    public Block createBreakableWall(int x, int y, int tileSize) {
        return new IceBreakableWall(x, y, tileSize);
    }

    @Override
    public Block createPassage(int x, int y, int tileSize) {
        return new IcePassage(x, y, tileSize);
    }

    @Override
    public Decoration createDecoration() {
        return new Decoration(0, 0, new DecorationStyle("SNOWMAN", Color.WHITE));
    }

}