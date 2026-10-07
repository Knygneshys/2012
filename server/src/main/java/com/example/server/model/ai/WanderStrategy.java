package com.example.server.model.ai;

import com.example.server.GameConstants;

import java.util.Random;

/**
 * Ambles about, mostly keeping its heading and turning only occasionally.
 * <p>
 * This is the behaviour the NPC had before strategies existed, lifted out of
 * {@link ServerNpc} unchanged: hold a heading, count down a timer, and on
 * expiry turn with {@link GameConstants#NPC_TURN_CHANCE}. Keeping the original
 * algorithm as one strategy among several is the point — it is now swappable
 * rather than baked in.
 * <p>
 * Walking into a wall turns it immediately rather than waiting out the timer,
 * which is both what a wanderer would do and what stops it leaning into the
 * obstacle for the rest of its run.
 */
public class WanderStrategy implements NpcStrategy {

    private final Random random;
    private int direction;
    private int turnTimerMs;

    public WanderStrategy(Random random) {
        this.random = random;
        this.direction = random.nextInt(DIRECTIONS.length);
        this.turnTimerMs = nextDelayMs();
    }

    @Override
    public int chooseStep(NpcContext context) {
        turnTimerMs -= context.deltaMs();
        boolean timerExpired = turnTimerMs <= 0;
        boolean walkedIntoWall = context.isBlocked(direction);

        if (walkedIntoWall
                || (timerExpired && context.random().nextDouble() < GameConstants.NPC_TURN_CHANCE)) {
            turnTimerMs = nextDelayMs();
            direction = newHeading(context.blockedDirection());
        }
        return direction;
    }

    @Override
    public String description() {
        return "Wanderer";
    }

    /**
     * A fresh heading that is not the wall just hit, so the NPC does not simply
     * try the same blocked step again next tick.
     */
    private int newHeading(int blocked) {
        int candidate = random.nextInt(DIRECTIONS.length);
        if (candidate != blocked) {
            return candidate;
        }
        return (candidate + 1 + random.nextInt(DIRECTIONS.length - 1)) % DIRECTIONS.length;
    }

    private int nextDelayMs() {
        return GameConstants.NPC_TURN_MIN_MS
                + random.nextInt(GameConstants.NPC_TURN_MAX_MS - GameConstants.NPC_TURN_MIN_MS + 1);
    }
}