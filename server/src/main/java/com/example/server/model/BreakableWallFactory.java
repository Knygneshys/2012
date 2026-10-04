package com.example.server.model;

public final class BreakableWallFactory extends ServerBlockFactory {
    @Override
    protected ServerBlock createBlock(int x, int y) {
        return new ServerBreakableWall(x, y);
    }
}
