package com.example.server.model;

/**
 * Empty walkable ground: never blocked, never destroyed, and what a
 * {@link ServerBreakableWall} leaves behind.
 */
public class ServerPassage extends ServerBlock {

    public ServerPassage(int x, int y) {
        super(x, y, TileType.FLOOR);
    }

    @Override
    public boolean isPassable() {
        return true;
    }

    @Override
    public boolean stopsBlast() {
        return false;
    }

    @Override
    public boolean isDestructible() {
        return false;
    }
}