package com.example.server.network;

import java.util.ArrayList;
import java.util.List;

/**
 * Serializable representation of the complete game state (server copy).
 */
public class GameStateData {
    public int[][] map; // TileType ordinal
    public List<PlayerData> players = new ArrayList<>();
    public List<BombData> bombs = new ArrayList<>();
    public List<ExplosionData> explosions = new ArrayList<>();
    public List<NpcData> npcs = new ArrayList<>();
    public List<PowerupData> powerups = new ArrayList<>();

    public GameStateData() {
    }

    public static class PlayerData {
        public int playerId;
        public String name;
        public int x;
        public int y;
        public boolean alive;
        public String colorHex;
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

    public static class NpcData {
        public int npcId;
        public int x;
        public int y;
        public int moveSpeed;
        public boolean alive;
        public String colorHex;

        public NpcData() {
        }

        public NpcData(int npcId, int x, int y, int moveSpeed, boolean alive, String colorHex) {
            this.npcId = npcId;
            this.x = x;
            this.y = y;
            this.moveSpeed = moveSpeed;
            this.alive = alive;
            this.colorHex = colorHex;
        }
    }

    public static class PowerupData {
        public int kind; // ServerPowerup.Kind ordinal
        public int tileX;
        public int tileY;

        public PowerupData() {
        }

        public PowerupData(int kind, int tileX, int tileY) {
            this.kind = kind;
            this.tileX = tileX;
            this.tileY = tileY;
        }
    }
}
