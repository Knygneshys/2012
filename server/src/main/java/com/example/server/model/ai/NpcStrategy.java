package com.example.server.model.ai;

/**
 * How one NPC decides which way to step on a given tick.
 * <p>
 * A Strategy in the GoF sense: a family of algorithms behind one interface, so
 * the behaviour varies independently of the thing using it. {@link ServerNpc}
 * <em>has</em> a strategy rather than <em>is</em> one of a subclass per
 * behaviour, which is what keeps a fifth personality from turning into a fifth
 * class in an inheritance tree.
 * <p>
 * The split of responsibility is deliberate. A strategy answers the whole
 * question of where to go, including what to do about a wall: {@link
 * NpcContext#blockedDirection()} tells it which direction was refused last tick,
 * and answering with a different one is how it slides along an obstacle instead
 * of vibrating against it. {@link ServerNpc} still owns the only thing that
 * genuinely has to be singular — the collision rule, through
 * {@link ServerCharacter#move} — and it owns nothing else. It reports the
 * outcome of a step and lets the strategy decide what to try next.
 * <p>
 * Implementations are free to hold state across ticks, since one strategy
 * instance belongs to one NPC for as long as it keeps it.
 */
public interface NpcStrategy {

    /**
     * Returned by {@link #chooseStep} when the NPC intends to stay where it is.
     */
    int HOLD = -1;

    /**
     * In {@link NpcContext#blockedDirection()} when the last step was not
     * refused. Distinct from {@link #HOLD}, which means the NPC chose not to
     * move at all.
     */
    int NO_BLOCK = -2;

    /**
     * The four steps an NPC can take, indexed by the value a strategy returns:
     * right, left, down, up.
     */
    int[][] DIRECTIONS = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

    /**
     * Picks the direction to step in, as an index into {@link #DIRECTIONS}, or
     * {@link #HOLD}. Called once per game tick, so implementations should stay
     * cheap and must not assume the NPC actually got its way.
     */
    int chooseStep(NpcContext context);

    /**
     * A short name for the log and for the status view.
     */
    String description();
}