package com.example;

import java.util.ArrayList;
import java.util.List;

public class Explosion {
    public static final int DEFAULT_DURATION_MS = 700;
    public final List<int[]> tiles = new ArrayList<>(); // each int[] is {x,y} in tile coords
    private int remainingMs;

    public Explosion() {
        this.remainingMs = DEFAULT_DURATION_MS;
    }

    public void tick(int deltaMs) {
        remainingMs -= deltaMs;
    }

    public boolean isExpired() {
        return remainingMs <= 0;
    }

    public void addTile(int tx, int ty) {
        tiles.add(new int[]{tx, ty});
    }
}
