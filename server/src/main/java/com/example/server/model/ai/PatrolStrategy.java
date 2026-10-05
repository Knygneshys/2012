package com.example.server.model.ai;

import java.util.Random;

/**
 * Walks back and forth along one axis instead of turning at random.
 * <p>
 * Where the wanderer produces a shapeless drift, this produces a predictable
 * beat: a guard pacing a corridor. The axis is picked once, so two patrols on
 * the same map tend to cover different lines rather than mirroring each other.
 * <p>
 * The course length is counted in <em>tiles walked</em>, which is the part that
 * has to be right for a patrol to look like a patrol. An NPC covers one pixel per
 * tick and a tile is forty, so a course measured in ticks is a few pixels of
 * walking: the patroller turns around inside a single tile and vibrates there at
 * the tick rate, never reaching the end of anything. Counting tiles also means
 * the turnaround lands on a tile boundary rather than on a scattered individual
 * tick, and the draw happens once per traverse rather than once per tick — the
 * latter compares the same counter against a different threshold every time and
 * turns at random points along the run.
 * <p>
 * Walls are remembered per direction rather than only for the step just
 * refused. The previous version consulted {@link NpcContext#isBlocked} for the
 * heading it was about to use, but turning swaps the two headings, so the
 * evidence that the old forward was a wall no longer described the new forward
 * and the patroller asked for it again. In a gap one tile wide on its axis that
 * is an endless alternation between two walls, and it never discovers that the
 * other axis is open. Each direction now carries its own flag, the flags swap
 * with the headings, and the axis is changed only once both of them are known to
 * be walls. A flag is dropped as soon as the NPC reaches a new tile, because
 * that is proof the wall it described is behind it.
 */
public class PatrolStrategy implements NpcStrategy {

    private static final int PATROL_MIN_TILES = 2;
    private static final int PATROL_TILES_RANGE = 4;

    private final Random random;

    private boolean horizontal;
    private int forwardIndex;
    private int reverseIndex;

    /**
     * Whether each way along the current axis has been refused at the tile the
     * NPC is standing on. Travels with its heading through
     * {@link #turnAround()}, and is cleared on arrival at a new tile.
     */
    private boolean forwardBlocked;
    private boolean reverseBlocked;

    /** Tiles left on the current traverse; zero or less means it is time to turn. */
    private int tilesLeft;

    private int lastTileX;
    private int lastTileY;
    private boolean placed;

    public PatrolStrategy(Random random) {
        this.random = random;
        this.horizontal = random.nextBoolean();
        pickHeadingOnAxis();
        this.tilesLeft = nextCourseLength();
        this.lastTileX = 0;
        this.lastTileY = 0;
        this.placed = false;
    }

    @Override
    public int chooseStep(NpcContext context) {
        countTileWalked(context);

        if (context.isBlocked(forwardIndex)) {
            forwardBlocked = true;
        }
        if (context.isBlocked(reverseIndex)) {
            reverseBlocked = true;
        }

        if (tilesLeft <= 0 || forwardBlocked) {
            turnAround();
        }
        if (forwardBlocked && reverseBlocked) {
            // Both ways along this axis are walls here, so turning cannot help
            // and would only alternate between them. Take the other axis, which
            // is what stops a patroller spawned in a one-wide gap from standing
            // still for the whole round.
            horizontal = !horizontal;
            pickHeadingOnAxis();
            forwardBlocked = false;
            reverseBlocked = false;
            turnAround();
        }
        return forwardIndex;
    }

    @Override
    public String description() {
        return "Patroller";
    }

    /**
     * Charges one tile of the course for arriving somewhere new, and treats that
     * arrival as proof the walls this strategy was worried about are behind it.
     * The first call only records where the NPC started, so spawning mid-tile
     * does not eat part of the course before it has moved at all.
     */
    private void countTileWalked(NpcContext context) {
        if (context.tileX() == lastTileX && context.tileY() == lastTileY) {
            return;
        }
        if (placed) {
            tilesLeft--;
            forwardBlocked = false;
            reverseBlocked = false;
        }
        placed = true;
        lastTileX = context.tileX();
        lastTileY = context.tileY();
    }

    /**
     * Swaps the two headings, carrying their blocked flags with them, and draws
     * the length of the next traverse. Triggered either by finishing a run or by
     * reaching the end of the corridor, which is what makes a patroller turn
     * around at a wall instead of grinding against it.
     */
    private void turnAround() {
        int swapIndex = forwardIndex;
        boolean swapBlocked = forwardBlocked;
        forwardIndex = reverseIndex;
        forwardBlocked = reverseBlocked;
        reverseIndex = swapIndex;
        reverseBlocked = swapBlocked;
        tilesLeft = nextCourseLength();
    }

    /**
     * Faces an arbitrary way along the current axis. A patroller that has just
     * changed axis must not simply reverse back out of the gap it walked into.
     */
    private void pickHeadingOnAxis() {
        forwardIndex = horizontal ? 0 : 2;
        reverseIndex = horizontal ? 1 : 3;
    }

    private int nextCourseLength() {
        return PATROL_MIN_TILES + random.nextInt(PATROL_TILES_RANGE);
    }
}
