package com.example.server.model;

import com.example.server.GameConstants;

import java.util.Random;

/**
 * Server side counterpart of the client NPC. Position, speed and the movement
 * rule come from {@link ServerCharacter}; what is left here is the wander
 * behaviour that decides which way to step.
 */
public class ServerNpc extends ServerCharacter {
    private static final int[][] DIRECTIONS = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

    public String colorHex;

    private int direction;
    private int directionTimerMs;
    private final Random random;

    public ServerNpc(int id, int x, int y, int moveSpeed, String colorHex, Random random) {
        super(id, x, y, moveSpeed);
        this.colorHex = colorHex;
        this.random = random;
        this.direction = random.nextInt(DIRECTIONS.length);
        this.directionTimerMs = nextTurnDelayMs();
    }

    /**
     * Advances the NPC by one tick.
     */
    public void tick(int deltaMs, ServerBlock[][] map) {
        if (!alive) return;

        directionTimerMs -= deltaMs;
        if (directionTimerMs <= 0) {
            directionTimerMs = nextTurnDelayMs();
            // Wander: usually keep going, sometimes turn.
            if (random.nextDouble() < GameConstants.NPC_TURN_CHANCE) {
                direction = random.nextInt(DIRECTIONS.length);
            }
        }

        if (!step(map)) {
            // Blocked, try the other way around instead of standing still.
            direction = (direction + 1 + random.nextInt(DIRECTIONS.length - 1)) % DIRECTIONS.length;
            step(map);
        }
    }

    /**
     * Moves one step in the current direction, if that is possible.
     */
    private boolean step(ServerBlock[][] map) {
        int[] dir = DIRECTIONS[direction];
        return move(dir[0] * moveSpeed, dir[1] * moveSpeed, map);
    }

    private int nextTurnDelayMs() {
        return GameConstants.NPC_TURN_MIN_MS + random.nextInt(GameConstants.NPC_TURN_MAX_MS - GameConstants.NPC_TURN_MIN_MS + 1);
    }
}