package com.example.server.model;

public class ServerBomb {
    public int tileX;
    public int tileY;
    public int remainingMs;
    public int radius;

    public ServerBomb(int tileX, int tileY, int fuseMs, int radius) {
        this.tileX = tileX;
        this.tileY = tileY;
        this.remainingMs = fuseMs;
        this.radius = radius;
    }

    public boolean tick(int deltaMs) {
        remainingMs -= deltaMs;
        return remainingMs <= 0;
    }
}
