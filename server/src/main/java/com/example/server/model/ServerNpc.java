package com.example.server.model;

import com.example.server.model.ai.NpcBehaviour;
import com.example.server.model.ai.NpcContext;
import com.example.server.model.ai.NpcStrategy;

import java.util.Random;

/**
 * Server side counterpart of the client NPC. Position, speed and the movement
 * rule come from {@link ServerCharacter}; what is left here is asking an
 * {@link NpcStrategy} where to go and reporting whether the map allowed it.
 * <p>
 * The decision belongs to the strategy: this class <em>has</em> one rather than
 * being one subclass per behaviour, so a new personality is a new strategy class
 * and an entry in {@link NpcBehaviour} instead of a new branch of the inheritance
 * tree. It can also be replaced while the NPC is alive, which is what
 * {@link #setBehaviour} is for.
 * <p>
 * There is deliberately no fallback step here. An earlier version re-rolled a
 * random direction whenever the chosen one was blocked, which looked fine in
 * open ground and made NPCs vibrate against walls at 100Hz — the correction
 * undid the progress the strategy had just made, so the pair fought each other.
 * Now the refusal is handed back to the strategy through
 * {@link NpcContext#blockedDirection()} and it picks a different way round,
 * which is the decision that actually belongs to it. If every option it offers
 * is a wall, the NPC simply stands still for that tick, which reads as a pause
 * rather than a shake. The one thing kept here is the collision rule itself,
 * because there must only ever be one copy of it.
 */
public class ServerNpc extends ServerCharacter {

    /**
     * How long an NPC may make no progress at all before its behaviour is
     * assumed to have painted itself into a corner. Half a second: long enough
     * that leaning against a wall on the way past does not count, short enough
     * that a pocketed NPC rejoins the game rather than standing there all round.
     */
    private static final int STUCK_TICKS_BEFORE_GIVING_UP = 50;

    public String colorHex;

    private final Random random;

    /**
     * The direction the map refused last tick, or {@link NpcStrategy#NO_BLOCK}.
     * Fed back to the strategy so it can answer differently instead of being
     * asked the same hopeless question every tick.
     */
    private int lastBlocked = NpcStrategy.NO_BLOCK;

    /**
     * Consecutive ticks on which the NPC did not move at all. Used only to spot
     * an NPC that has genuinely run out of options; a tick where the strategy
     * asks to hold does not count, since that is a decision rather than a
     * failure.
     */
    private int stuckTicks;

    private NpcBehaviour behaviour;
    private NpcStrategy strategy;

    /** An NPC with the default wandering behaviour. */
    public ServerNpc(int id, int x, int y, int moveSpeed, String colorHex, Random random) {
        this(id, x, y, moveSpeed, colorHex, NpcBehaviour.WANDERER, NpcBehaviour.WANDERER.create(random), random);
    }

    /**
     * The full form. The random source is taken rather than created here so a
     * test can step an NPC deterministically, and the strategy is handed in
     * because only {@link NpcBehaviour} knows how to build one.
     */
    public ServerNpc(int id, int x, int y, int moveSpeed, String colorHex,
                     NpcBehaviour behaviour, NpcStrategy strategy, Random random) {
        super(id, x, y, moveSpeed);
        this.colorHex = colorHex;
        this.behaviour = behaviour;
        this.strategy = strategy;
        this.random = random;
    }

    /**
     * Advances the NPC by one tick.
     *
     * @param deltaMs milliseconds since the previous tick
     * @param map     the authoritative map, the only thing that decides whether a
     *                step is possible
     * @param target  the living player this NPC reacts to, or null when there
     *                is nobody to react to
     */
    public void tick(int deltaMs, ServerBlock[][] map, ServerCharacter target) {
        if (!alive) return;

        if (target == null) {
            switchToFallbackIfUnsupported();
        }

        NpcContext context = (target == null
                ? NpcContext.withoutTarget(deltaMs, tileX(), tileY(), random)
                : new NpcContext(deltaMs, tileX(), tileY(), target.tileX(), target.tileY(), random))
                .withBlocked(lastBlocked);

        int wanted = strategy.chooseStep(context);
        if (wanted < 0) {
            lastBlocked = NpcStrategy.NO_BLOCK;
            stuckTicks = 0;
            return;
        }

        int[] dir = NpcStrategy.DIRECTIONS[wanted];
        if (move(dir[0] * moveSpeed, dir[1] * moveSpeed, map)) {
            lastBlocked = NpcStrategy.NO_BLOCK;
            stuckTicks = 0;
            return;
        }

        lastBlocked = wanted;
        if (++stuckTicks >= STUCK_TICKS_BEFORE_GIVING_UP) {
            stuckTicks = 0;
            NpcBehaviour fallback = behaviour.fallbackWhenStuck();
            if (fallback != behaviour) {
                setBehaviour(fallback);
            }
        }
    }

    /**
     * Swaps the behaviour of a live NPC. The old strategy goes away with
     * whatever heading or remaining course it was keeping, and so does the
     * record of the step the old one was told it could not make.
     */
    public void setBehaviour(NpcBehaviour newBehaviour) {
        this.behaviour = newBehaviour;
        this.strategy = newBehaviour.create(random);
        this.lastBlocked = NpcStrategy.NO_BLOCK;
    }

    public NpcBehaviour behaviour() {
        return behaviour;
    }

    /**
     * Drops to the behaviour's declared fallback once there is nobody left to
     * react to, so a chaser does not keep hunting an empty map.
     */
    private void switchToFallbackIfUnsupported() {
        NpcBehaviour fallback = behaviour.fallbackWithoutTarget();
        if (fallback != behaviour) {
            setBehaviour(fallback);
        }
    }
}