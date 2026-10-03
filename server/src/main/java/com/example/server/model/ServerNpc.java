package com.example.server.model;

import com.example.server.GameConstants;

import java.util.Random;

/**
 * Server side counterpart of the client NPC. Walks around the map, turns when
 * it hits something and is killed by bomb blasts.
 */
public class ServerNpc {
    private static final int[][] DIRECTIONS = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

    public final int id;
    public int x;
    public int y;
    public int moveSpeed;
    public String colorHex;
    public boolean alive = true;

    private int direction;
    private int directionTimerMs;
    private final Random random;

    public ServerNpc(int id, int x, int y, int moveSpeed, String colorHex, Random random) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.moveSpeed = moveSpeed;
        this.colorHex = colorHex;
        this.random = random;
        this.direction = random.nextInt(DIRECTIONS.length);
        this.directionTimerMs = nextTurnDelayMs();
    }

    /**
     * Advances the NPC by one tick.
     */
    public void tick(int deltaMs, TileType[][] map) {
        if (!alive) return;

        directionTimerMs -= deltaMs;
        if (directionTimerMs <= 0) {
            directionTimerMs = nextTurnDelayMs();
            // Wander: usually keep going, sometimes turn.
            if (random.nextDouble() < GameConstants.NPC_TURN_CHANCE) {
                direction = random.nextInt(DIRECTIONS.length);
            }
        }

        int[] dir = DIRECTIONS[direction];
        int dx = dir[0] * moveSpeed;
        int dy = dir[1] * moveSpeed;
        if (!move(dx, dy, map)) {
            // Blocked, try the other way around instead of standing still.
            direction = (direction + 1 + random.nextInt(DIRECTIONS.length - 1)) % DIRECTIONS.length;
            dir = DIRECTIONS[direction];
            move(dir[0] * moveSpeed, dir[1] * moveSpeed, map);
        }
    }

    public void kill() {
        alive = false;
    }

    public int tileX() {
        return x / GameConstants.TILE_SIZE;
    }

    public int tileY() {
        return y / GameConstants.TILE_SIZE;
    }

    private int nextTurnDelayMs() {
        return GameConstants.NPC_TURN_MIN_MS + random.nextInt(GameConstants.NPC_TURN_MAX_MS - GameConstants.NPC_TURN_MIN_MS + 1);
    }

    /**
     * Moves by (dx, dy) when the destination is inside the map and on floor.
     * Returns true when the NPC moved.
     */
    private boolean move(int dx, int dy, TileType[][] map) {
        if (dx == 0 && dy == 0) return false;

        int newX = x + dx;
        int newY = y + dy;
        int maxX = map[0].length * GameConstants.TILE_SIZE;
        int maxY = map.length * GameConstants.TILE_SIZE;
        if (newX < 0 || newY < 0 || newX + GameConstants.TILE_SIZE > maxX
            || newY + GameConstants.TILE_SIZE > maxY) {
            return false;
        }

        // Collision check with the four corners of the body.
        if (!isFloor(map, newX, newY)
            || !isFloor(map, newX + GameConstants.TILE_SIZE - 1, newY)
            || !isFloor(map, newX, newY + GameConstants.TILE_SIZE - 1)
            || !isFloor(map, newX + GameConstants.TILE_SIZE - 1, newY + GameConstants.TILE_SIZE - 1)) {
            return false;
        }

        x = newX;
        y = newY;
        return true;
    }

    private static boolean isFloor(TileType[][] map, int px, int py) {
        int tileX = px / GameConstants.TILE_SIZE;
        int tileY = py / GameConstants.TILE_SIZE;
        if (tileX < 0 || tileY < 0 || tileY >= map.length || tileX >= map[0].length) {
            return false;
        }
        return map[tileY][tileX] == TileType.FLOOR;
    }
}