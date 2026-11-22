package com.example.server.model.ai;

/**
 * Keeps away from the nearest living player.
 * <p>
 * The inverse of {@link ChaseStrategy}: the step sign is flipped so the NPC
 * opens the distance rather than closing it. On equal-distance diagonals it
 * tries the vertical axis first; otherwise both use the same movement sequence.
 * Keeping them as separate strategies lets a map mix hunters and cowards
 * and swap either at runtime.
 * <p>
 * A coward with nowhere to back away from holds its ground rather than stepping
 * into the wall it is already up against — a correction that pushed it towards
 * the player would be the opposite of what it wants, and is exactly the mistake
 * that leaves an NPC vibrating in a corner. That includes the case where the
 * player is exactly on one axis and there is no sideways step available: see
 * {@link ChaseStrategy} for why stepping off perpendicular there is worse than
 * standing still.
 */
public class CowardStrategy extends TargetStrategy {

    @Override
    protected int stepFor(int delta) {
        return -Integer.signum(delta);
    }

    @Override
    protected boolean preferHorizontalOnTie() {
        return false;
    }

    @Override
    public String description() {
        return "Coward";
    }
}
