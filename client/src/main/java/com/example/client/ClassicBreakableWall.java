package com.example.client;

public class ClassicBreakableWall extends BreakableWall {

    public ClassicBreakableWall(int tileX, int tileY, int tileSize) {
        super(tileX, tileY, tileSize);
    }

    @Override
    public Block destroyed() {
        return new ClassicPassage(tileX, tileY, tileSize);
    }
}