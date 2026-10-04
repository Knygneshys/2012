package com.example.server.model;

/**
 * Places tiles using a Factory Method implemented by each concrete creator.
 */
public abstract class ServerBlockFactory {
    public final void placeBlock(ServerBlock[][] map, int x, int y) {
        map[y][x] = createBlock(x, y);
    }

    protected abstract ServerBlock createBlock(int x, int y);
}
