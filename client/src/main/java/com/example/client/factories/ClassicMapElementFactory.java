package com.example.client.factories;

import com.example.client.Block;
import com.example.client.ClassicWall;
import com.example.client.ClassicBreakableWall;
import com.example.client.ClassicPassage;

public class ClassicMapElementFactory implements MapElementFactory {

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
}