package com.example.server.model;

import java.util.ArrayList;
import java.util.List;

public class ServerExplosion {
    public List<int[]> tiles = new ArrayList<>();
    public int remainingMs = 700;

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
