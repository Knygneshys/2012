package com.example.server.model.ai;

/**
 * Never moves. A fixed obstacle that is nonetheless a character: it still
 * blocks a tile, can still be caught in a blast, and still shows up in the
 * state broadcast.
 * <p>
 * Worth having as a strategy rather than a flag because "this NPC does not
 * move" is then just another interchangeable behaviour. It also gives the
 * behaviour set a degenerate case to prove the contract holds: a strategy is
 * allowed to answer {@link NpcStrategy#HOLD} forever and the NPC handles it
 * without special-casing anything.
 */
public class SentinelStrategy implements NpcStrategy {

    @Override
    public int chooseStep(NpcContext context) {
        return HOLD;
    }

    @Override
    public String description() {
        return "Sentinel";
    }
}