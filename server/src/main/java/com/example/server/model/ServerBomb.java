package com.example.server.model;

public class ServerBomb {
    public int tileX;
    public int tileY;
    public int remainingMs;
    public int radius;
    /**
     * Id of the player that placed the bomb, used to enforce their bomb limit.
     */
    public int ownerId = -1;

    public ServerBomb(int tileX, int tileY, int fuseMs, int radius) {
        this(tileX, tileY, fuseMs, radius, -1);
    }

    public ServerBomb(int tileX, int tileY, int fuseMs, int radius, int ownerId) {
        this.tileX = tileX;
        this.tileY = tileY;
        this.remainingMs = fuseMs;
        this.radius = radius;
        this.ownerId = ownerId;
    }

    public boolean tick(int deltaMs) {
        remainingMs -= deltaMs;
        return remainingMs <= 0;
    }
}