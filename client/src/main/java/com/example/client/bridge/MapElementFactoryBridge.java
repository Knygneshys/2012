package com.example.client.bridge;

import com.example.client.Block;
import com.example.client.factories.MapElementFactory;

public class MapElementFactoryBridge implements MapElementFactory {
    private final TileTheme tileTheme;

    public MapElementFactoryBridge(TileTheme tileTheme) {
        this.tileTheme = tileTheme;
    }

    @Override
    public Block createWall(int x, int y, int tileSize) {
        return tileTheme.createWall(x, y, tileSize);
    }

    @Override
    public Block createBreakableWall(int x, int y, int tileSize) {
        return tileTheme.createBreakableWall(x, y, tileSize);
    }

    @Override
    public Block createPassage(int x, int y, int tileSize) {
        return tileTheme.createPassage(x, y, tileSize);
    }
}

