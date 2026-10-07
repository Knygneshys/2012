package com.example.server.model.ai;

import java.util.Random;

/**
 * The catalogue of behaviours an {@link ServerNpc} can be given, and the factory
 * that builds them.
 * <p>
 * This is where the question "who chooses the strategy?" gets answered. It is
 * deliberately not the NPC and not the strategy: the server decides at spawn
 * time which personalities a round gets, so the NPC only ever reacts to the
 * world in front of it. Adding a sixth behaviour means adding a constant here
 * and one class, and touching neither {@link ServerNpc} nor the other
 * strategies.
 * <p>
 * {@link #fallbackWithoutTarget()} covers the other half of the question, the
 * runtime switch: a behaviour that needs a player to react to says what it
 * should become once there are none left, so an NPC does not have to keep
 * re-deciding that it is pointless to keep hunting an empty map.
 */
public enum NpcBehaviour {

    WANDERER("Wanderer", false, false) {
        @Override
        public NpcStrategy create(Random random) {
            return new WanderStrategy(random);
        }
    },
    PATROLLER("Patroller", false, false) {
        @Override
        public NpcStrategy create(Random random) {
            return new PatrolStrategy(random);
        }
    },
    CHASER("Chaser", true, true) {
        @Override
        public NpcStrategy create(Random random) {
            return new ChaseStrategy();
        }
    },
    COWARD("Coward", true, true) {
        @Override
        public NpcStrategy create(Random random) {
            return new CowardStrategy();
        }
    },
    SENTINEL("Sentinel", false, false) {
        @Override
        public NpcStrategy create(Random random) {
            return new SentinelStrategy();
        }
    };

    private final String displayName;
    private final boolean needsTarget;
    private final boolean givesUpWhenStuck;

    NpcBehaviour(String displayName, boolean needsTarget, boolean givesUpWhenStuck) {
        this.displayName = displayName;
        this.needsTarget = needsTarget;
        this.givesUpWhenStuck = givesUpWhenStuck;
    }

    /**
     * Builds a fresh strategy for one NPC. Each call returns a new object
     * because the stateful behaviours — a wanderer's heading, a patrol's
     * remaining course — belong to the NPC holding them, not to the enum.
     */
    public abstract NpcStrategy create(Random random);

    /**
     * What an NPC should become when no living player is left to react to.
     * Behaviours that never needed a target report themselves, which makes the
     * switch in {@link ServerNpc} a no-op for them.
     */
    public NpcBehaviour fallbackWithoutTarget() {
        return needsTarget ? WANDERER : this;
    }

    /**
     * What an NPC should become when its behaviour has run into a pocket it
     * cannot get out of on its own.
     * <p>
     * Only the target-seeking behaviours give up. Chasing is greedy, so an NPC
     * whose target is walled off on both axes has no move left that makes sense
     * and would stand still for the rest of the round; dropping to the wanderer
     * gets it walking again. A patroller deals with walls itself by changing
     * axis, a wanderer already turns, and a sentinel is stationary on purpose.
     */
    public NpcBehaviour fallbackWhenStuck() {
        return givesUpWhenStuck ? WANDERER : this;
    }

    public String displayName() {
        return displayName;
    }

    /**
     * The behaviour for the nth NPC of a round, cycling through the catalogue so
     * a short round still shows a mix of personalities.
     */
    public static NpcBehaviour forIndex(int index) {
        NpcBehaviour[] all = values();
        return all[Math.floorMod(index, all.length)];
    }
}