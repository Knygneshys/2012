package com.example.server.model;

import com.example.server.GameConstants;

public class ServerPlayer {
    public int id;
    public String name;
    public int x, y;
    public int moveSpeed = 10;
    public String colorHex;
    public boolean alive = true;
    private final int spawnX;
    private final int spawnY;

    public ServerPlayer(int id, String name, int x, int y, String colorHex) {
        this.id = id;
        this.name = name;
        this.x = x;
        this.y = y;
        this.spawnX = x;
        this.spawnY = y;
        this.colorHex = colorHex;
    }

    public void move(int dx, int dy, TileType[][] map) {
        if (!alive) return;
        if (x + dx < 0 || x + dx >= map[0].length * GameConstants.TILE_SIZE ||
            y + dy < 0 || y + dy >= map.length * GameConstants.TILE_SIZE) {
            return;
        }

        int newX = x + dx;
        int newY = y + dy;

        // Collision check with 4 corners
        int tileX = newX / GameConstants.TILE_SIZE;
        int tileY = newY / GameConstants.TILE_SIZE;
        int tileX1 = (newX + GameConstants.TILE_SIZE - 1) / GameConstants.TILE_SIZE;
        int tileY1 = (newY + GameConstants.TILE_SIZE - 1) / GameConstants.TILE_SIZE;

        if (map[tileY][tileX] != TileType.FLOOR || map[tileY][tileX1] != TileType.FLOOR ||
            map[tileY1][tileX] != TileType.FLOOR || map[tileY1][tileX1] != TileType.FLOOR) {
            return;
        }

        x = newX;
        y = newY;
    }

    public void reset() {
        x = spawnX;
        y = spawnY;
        alive = true;
    }
}
