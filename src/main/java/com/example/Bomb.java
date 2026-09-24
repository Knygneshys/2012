package com.example;

public class Bomb {
    public final int tileX;
    public final int tileY;
    private int remainingMs;
    public final int radius;

    public Bomb(int tileX, int tileY, int fuseMs, int radius) {
        this.tileX = tileX;
        this.tileY = tileY;
        this.remainingMs = fuseMs;
        this.radius = radius;
    }

    /**
     * Tick the bomb. Returns true if it just detonated.
     */
    public boolean tick(int deltaMs) {
        remainingMs -= deltaMs;
        return remainingMs <= 0;
    }
}
