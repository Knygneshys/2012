package com.example.server.model;

/**
 * A destructible block. A blast consumes it, leaving a {@link ServerPassage},
 * and the tile it stood on may drop a powerup.
 */
public class ServerBreakableWall extends ServerBlock {

    public ServerBreakableWall(int x, int y) {
        super(x, y, TileType.SOFT_BLOCK);
    }

    @Override
    public boolean isPassable() {
        return false;
    }

    @Override
    public boolean stopsBlast() {
        return true;
    }

    @Override
    public boolean isDestructible() {
        return true;
    }

    @Override
    public ServerBlock destroyed() {
        return new ServerPassage(x, y);
    }
}