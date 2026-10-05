package com.example.server.model.ai;

import java.util.Random;

/**
 * Everything a strategy is allowed to see about the world for one tick.
 * <p>
 * Deliberately narrow. It carries no map, because asking a strategy to reason
 * about walls would put a second, subtly different copy of the collision rule
 * next to the real one in {@link ServerNpc}. A strategy is only told <em>that</em>
 * a direction was refused, never why, and picks its own way around.
 *
 * @param deltaMs         milliseconds since the previous tick, for strategies that
 *                        count time such as the wanderer's turn timer
 * @param tileX           the NPC's own tile column
 * @param tileY           the NPC's own tile row
 * @param targetTileX     the target's tile column, or -1 when there is no target
 * @param targetTileY     the target's tile row, or -1 when there is no target
 * @param random          shared randomness, passed in so a strategy can be built
 *                        and stepped deterministically in a test
 * @param blockedDirection the direction the NPC asked for last tick and the map
 *                        refused, or {@link NpcStrategy#NO_BLOCK}. This is the
 *                        whole of the obstacle feedback, and answering it
 *                        properly is what stops a strategy from being walked
 *                        back and forth across the same tile.
 */
public record NpcContext(
        int deltaMs,
        int tileX,
        int tileY,
        int targetTileX,
        int targetTileY,
        Random random,
        int blockedDirection) {

    /**
     * Convenience form for a context where nothing has been refused yet.
     */
    public NpcContext(int deltaMs, int tileX, int tileY,
                      int targetTileX, int targetTileY, Random random) {
        this(deltaMs, tileX, tileY, targetTileX, targetTileY, random, NpcStrategy.NO_BLOCK);
    }

    /**
     * A context for an NPC with nobody to react to, such as an empty lobby.
     */
    public static NpcContext withoutTarget(int deltaMs, int tileX, int tileY, Random random) {
        return new NpcContext(deltaMs, tileX, tileY, -1, -1, random, NpcStrategy.NO_BLOCK);
    }

    /**
     * A copy carrying the outcome of the previous step. Records are immutable,
     * so this returns a new context rather than changing this one.
     */
    public NpcContext withBlocked(int direction) {
        return new NpcContext(deltaMs, tileX, tileY, targetTileX, targetTileY, random, direction);
    }

    /**
     * True when the given direction is the one the map refused last tick.
     */
    public boolean isBlocked(int direction) {
        return blockedDirection == direction;
    }

    /**
     * True when there is a living player for this NPC to move towards or away
     * from. Strategies that ignore the target can ignore this too.
     */
    public boolean hasTarget() {
        return targetTileX >= 0 && targetTileY >= 0;
    }

    /**
     * Horizontal distance to the target, signed towards it.
     */
    public int deltaX() {
        return targetTileX - tileX;
    }

    /**
     * Vertical distance to the target, signed towards it.
     */
    public int deltaY() {
        return targetTileY - tileY;
    }

    /**
     * The index in {@link NpcStrategy#DIRECTIONS} for a signed step, so the
     * target-seeking strategies do not each re-derive the mapping.
     */
    public static int directionIndex(int stepX, int stepY) {
        if (stepX > 0) return 0;
        if (stepX < 0) return 1;
        if (stepY > 0) return 2;
        if (stepY < 0) return 3;
        return NpcStrategy.HOLD;
    }
}