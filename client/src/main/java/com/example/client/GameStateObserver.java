package com.example.client;

/**
 * Something that wants to hear about every server update.
 * <p>
 * The Observer half of the pattern. An observer knows only that updates exist
 * and what they look like; it has no reference to the subject and no way to
 * make another update happen. That is the whole point — the alternative is the
 * one object that receives the update having to know about the map, the NPCs,
 * the powerups and the players by name and call into each of them, which is
 * exactly the dependency pile-up that made
 * {@code GameController.applyGameState} a single fifty-line method.
 */
public interface GameStateObserver {

    /**
     * Called once per server broadcast, on the event dispatch thread.
     * <p>
     * An observer that throws would stop the ones after it, so implementations
     * are expected to handle their own failures rather than rely on the subject.
     */
    void onGameState(ClientGameState state);

    /**
     * A short label, used when logging the fan-out so it is clear which
     * observers took part. Defaults to the class name, which is the right
     * answer for every observer that has nothing better to say.
     * <p>
     * Having a default is also what keeps this a functional interface, so an
     * observer can still be written as a lambda for a one-off case.
     */
    default String name() {
        return getClass().getSimpleName();
    }
}