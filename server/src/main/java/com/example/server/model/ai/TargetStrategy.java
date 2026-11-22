package com.example.server.model.ai;

/** Template Method for target-reacting NPCs: subclasses choose the step direction. */
abstract class TargetStrategy implements NpcStrategy {

    @Override
    public final int chooseStep(NpcContext context) {
        if (!context.hasTarget()) {
            return HOLD;
        }

        int dx = context.deltaX();
        int dy = context.deltaY();
        if (dx == 0 && dy == 0) {
            return HOLD;
        }

        boolean horizontalFirst = Math.abs(dx) > Math.abs(dy)
                || (Math.abs(dx) == Math.abs(dy) && preferHorizontalOnTie());
        int preferred = direction(horizontalFirst ? dx : 0, horizontalFirst ? 0 : dy);
        if (!context.isBlocked(preferred)) {
            return preferred;
        }
        return direction(horizontalFirst ? 0 : dx, horizontalFirst ? dy : 0);
    }

    private int direction(int dx, int dy) {
        return NpcContext.directionIndex(stepFor(dx), stepFor(dy));
    }

    /** Required step: turn a target offset into a movement step. */
    protected abstract int stepFor(int delta);

    /** Optional hook: which axis to try first when both distances are equal. */
    protected boolean preferHorizontalOnTie() {
        return true;
    }
}
