package com.example.network;

import java.util.ArrayList;
import java.util.List;

/**
 * Serializable representation of the complete game state.
 */
public class GameStateData {
    public int[][] map; // TileType as int
    public List<PlayerData> players = new ArrayList<>();
    public List<BombData> bombs = new ArrayList<>();
    public List<ExplosionData> explosions = new ArrayList<>();

    public GameStateData() {
    }

    public static class PlayerData {
        public int playerId;
        public String name;
        public int x;
        public int y;
        public boolean alive;
        public String colorHex; // hex color representation
        public int moveSpeed;

        public PlayerData() {
        }

        public PlayerData(int playerId, String name, int x, int y, boolean alive, String colorHex, int moveSpeed) {
            this.playerId = playerId;
            this.name = name;
            this.x = x;
            this.y = y;
            this.alive = alive;
            this.colorHex = colorHex;
            this.moveSpeed = moveSpeed;
        }
    }

    public static class BombData {
        public int tileX;
        public int tileY;
        public int remainingMs;
        public int radius;

        public BombData() {
        }

        public BombData(int tileX, int tileY, int remainingMs, int radius) {
            this.tileX = tileX;
            this.tileY = tileY;
            this.remainingMs = remainingMs;
            this.radius = radius;
        }
    }

    public static class ExplosionData {
        public int[][] tiles; // each int[2] is {x, y} in tile coords
        public int remainingMs;

        public ExplosionData() {
        }

        public ExplosionData(int[][] tiles, int remainingMs) {
            this.tiles = tiles;
            this.remainingMs = remainingMs;
        }
    }
}

