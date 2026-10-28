package com.example.client.bridge;

import com.example.client.Block;
import com.example.client.ClassicBreakableWall;
import com.example.client.ClassicPassage;
import com.example.client.ClassicWall;
import com.example.client.Decoration;
import com.example.client.DecorationStyle;
import com.example.client.ClassicDecoration;
import java.awt.Color;

public class ClassicTileTheme implements TileTheme {
    @Override
    public Block createWall(int x, int y, int tileSize) {
        return new ClassicWall(x, y, tileSize);
    }

    @Override
    public Block createBreakableWall(int x, int y, int tileSize) {
        return new ClassicBreakableWall(x, y, tileSize);
    }

    @Override
    public Block createPassage(int x, int y, int tileSize) {
        return new ClassicPassage(x, y, tileSize);
    }


    @Override
    public Decoration createDecoration() {
        return new ClassicDecoration(0, 0, new DecorationStyle("BARREL", new Color(150, 85, 45)));
    }


}

