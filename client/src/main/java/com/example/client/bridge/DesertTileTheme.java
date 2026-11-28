package com.example.client.bridge;

import com.example.client.Block;
import com.example.client.Decoration;
import com.example.client.DecorationStyle;
import com.example.client.DesertBreakableWall;
import com.example.client.DesertDecoration;
import com.example.client.DesertPassage;
import com.example.client.DesertWall;

import java.awt.Color;

/** Sand floor, sandstone walls, crates to blow up and cacti for decoration. */
public class DesertTileTheme implements TileTheme {
    @Override
    public Block createWall(int x, int y, int tileSize) {
        return new DesertWall(x, y, tileSize);
    }

    @Override
    public Block createBreakableWall(int x, int y, int tileSize) {
        return new DesertBreakableWall(x, y, tileSize);
    }

    @Override
    public Block createPassage(int x, int y, int tileSize) {
        return new DesertPassage(x, y, tileSize);
    }

    @Override
    public Decoration createDecoration() {
        return new DesertDecoration(0, 0, new DecorationStyle("CACTUS", new Color(70, 150, 70)));
    }
}
