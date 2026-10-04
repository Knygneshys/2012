package com.example.client;

public class Bomb extends GameObject {
    public static final int DEFAULT_FUSE_MS = 2000;
    public static final int DEFAULT_RADIUS = 2;

    public final int tileX;
    public final int tileY;
    public final int radius;
    /**
     * Id of the character that placed the bomb, or -1 for a bomb that belongs
     * to nobody. Used to enforce the per player bomb limit.
     */
    public final int ownerId;
    private int remainingMs;

    public Bomb(int tileX, int tileY, int fuseMs, int radius) {
        this(tileX, tileY, fuseMs, radius, -1, MapPanel.TILE_SIZE);
    }

    public Bomb(int tileX, int tileY, int fuseMs, int radius, int ownerId, int tileSize) {
        super(tileX * tileSize, tileY * tileSize, tileSize, tileSize);
        this.tileX = tileX;
        this.tileY = tileY;
        this.remainingMs = fuseMs;
        this.radius = radius;
        this.ownerId = ownerId;
    }

    public int getRemainingMs() {
        return remainingMs;
    }

    /**
     * Tick the bomb. Returns true if it just detonated.
     */
    public boolean tick(int deltaMs) {
        remainingMs -= deltaMs;
        return remainingMs <= 0;
    }
}