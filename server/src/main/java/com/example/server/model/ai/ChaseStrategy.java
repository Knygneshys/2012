package com.example.server.model.ai;

/**
 * Closes on the nearest living player.
 * <p>
 * Walks the axis it is furthest away on, which is the greedy step towards a
 * target on a tile grid and needs no pathfinding. When that step is refused —
 * {@link NpcContext#blockedDirection()} says so — it closes on the <em>other</em>
 * axis instead.
 * <p>
 * That second part is what makes it usable rather than jittery. Greedily
 * re-deciding from the current tile alone makes an NPC walk into a wall, and any
 * correction applied afterwards tends to push it back across the tile boundary
 * it just crossed, at which point it greedily wants to go forward again. The
 * result is an NPC vibrating on one tile a hundred times a second. Holding the
 * blocked direction in view and switching axis instead keeps it closing.
 * <p>
 * The target arrives pre-selected in {@link NpcContext}, so this strategy never
 * sees the player list and cannot start caring how many players there are.
 * <p>
 * When the target is exactly on one axis there is no second axis to close on,
 * and the case is worth being explicit about. The player is then straight ahead
 * behind whatever refused the step, so there is no direction that both makes
 * progress and is not a wall. Answering {@link #HOLD} says the chaser is
 * deliberately standing its ground until the geometry changes, which is what
 * happens the moment the player steps to one side or a blast opens the way.
 * Answering a perpendicular step instead is worse than useless: the sideways move
 * clears the record of the refusal, so on arriving back the greedy step wants the
 * same wall again, is refused again, and sidesteps again — an NPC pacing between
 * two tiles at the tick rate, which is the exact failure this class of bug
 * produces and the one this whole design exists to avoid.
 */
public class ChaseStrategy implements NpcStrategy {

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
        int stepX = horizontalFirst ? Integer.signum(dx) : 0;
        int stepY = horizontalFirst ? 0 : Integer.signum(dy);

        int towards = NpcContext.directionIndex(stepX, stepY);
        if (!context.isBlocked(towards)) {
            return towards;
        }

        // The way towards the target is a wall, so close on the other axis.
        int sideways = NpcContext.directionIndex(
                horizontalFirst ? 0 : Integer.signum(dx),
                horizontalFirst ? Integer.signum(dy) : 0);
        return sideways;
    }

    @Override
    public String description() {
        return "Chaser";
    }
}
