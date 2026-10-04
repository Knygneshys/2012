package com.example.server.model;

public final class WallFactory extends ServerBlockFactory {
    @Override
    protected ServerBlock createBlock(int x, int y) {
        return new ServerWall(x, y);
    }
}
