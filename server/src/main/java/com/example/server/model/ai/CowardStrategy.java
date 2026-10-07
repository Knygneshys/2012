package com.example.server.model.ai;

/**
 * Keeps away from the nearest living player.
 * <p>
 * The exact inverse of {@link ChaseStrategy}: the same axis choice with the sign
 * flipped, so the NPC opens the distance rather than closing it, and the same
 * switch to the other axis when the way it wants to run is a wall. Having both
 * as separate strategies rather than a boolean flag on one class is what lets a
 * map mix hunters and cowards and lets either be swapped at runtime.
 * <p>
 * A coward with nowhere to back away from holds its ground rather than stepping
 * into the wall it is already up against — a correction that pushed it towards
 * the player would be the opposite of what it wants, and is exactly the mistake
 * that leaves an NPC vibrating in a corner. That includes the case where the
 * player is exactly on one axis and there is no sideways step available: see
 * {@link ChaseStrategy} for why stepping off perpendicular there is worse than
 * standing still.
 */
public class CowardStrategy implements NpcStrategy {

    @Override
    public int chooseStep(NpcContext context) {
        if (!context.hasTarget()) {
            return HOLD;
        }
        int dx = context.deltaX();
        int dy = context.deltaY();
        if (dx == 0 && dy == 0) {
            return HOLD;
        }

        boolean horizontalFirst = Math.abs(dx) >= Math.abs(dy);
        int stepX = horizontalFirst ? -Integer.signum(dx) : 0;
        int stepY = horizontalFirst ? 0 : -Integer.signum(dy);

        int away = NpcContext.directionIndex(stepX, stepY);
        if (!context.isBlocked(away)) {
            return away;
        }

        int sideways = NpcContext.directionIndex(
                horizontalFirst ? 0 : -Integer.signum(dx),
                horizontalFirst ? -Integer.signum(dy) : 0);
        return sideways;
    }

    @Override
    public String description() {
        return "Coward";
    }
}
