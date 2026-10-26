package com.example.client.bridge;

import com.example.client.Block;

public interface TileTheme {
    Block createWall(int x, int y, int tileSize);

    Block createBreakableWall(int x, int y, int tileSize);

    Block createPassage(int x, int y, int tileSize);
}

