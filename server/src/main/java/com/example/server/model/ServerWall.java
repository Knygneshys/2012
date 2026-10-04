package com.example.server.model;

/**
 * A wall of the map border and lattice. Survives every blast and stops it.
 */
public class ServerWall extends ServerBlock {

    public ServerWall(int x, int y) {
        super(x, y, TileType.HARD_WALL);
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
        return false;
    }
}