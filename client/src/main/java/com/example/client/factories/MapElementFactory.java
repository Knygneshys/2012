package com.example.client.factories;

import com.example.client.Block;
import com.example.client.Decoration;

public interface MapElementFactory {

    Block createWall(int x, int y, int tileSize);

    Block createBreakableWall(int x, int y, int tileSize);

    Block createPassage(int x, int y, int tileSize);

    Decoration createDecoration();
}
