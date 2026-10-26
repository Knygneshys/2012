package com.example.client.bridge;

import com.example.client.Block;
import com.example.client.IceBreakableWall;
import com.example.client.IcePassage;
import com.example.client.IceWall;

public class IceTileTheme implements TileTheme {
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
}

