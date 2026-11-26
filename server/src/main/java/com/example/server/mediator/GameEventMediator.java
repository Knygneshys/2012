package com.example.server.mediator;

import com.example.server.model.ServerBomb;
import com.example.server.model.ServerNpc;
import com.example.server.model.ServerPlayer;
import com.example.server.model.ServerPowerup;

/**
 * Mediator interface for server-side game events.
 *
 * <p>The three primary colleagues are {@link ServerPlayer}, {@link ServerBomb},
 * and {@link ServerNpc}. Instead of referencing each other directly, they
 * notify this mediator and let it decide what happens next.
 *
 * <p>Contrast with Observer (already implemented as {@code GameStateSubject} on
 * the client side): Observer pushes a snapshot to passive listeners; Mediator
 * actively decides the consequences of events (who dies, whether the round ends,
 * what gets logged).
 */
public interface GameEventMediator {

    /**
     * Called when a bomb's fuse expires and it detonates.
     * The mediator resolves damage to players, NPCs and chain bombs.
     */
    void onBombExploded(ServerBomb bomb);

    /**
     * Called when a player is killed (by explosion or NPC contact).
     * The mediator may check win/loss conditions.
     */
    void onPlayerKilled(ServerPlayer player, String cause);

    /**
     * Called when an NPC physically touches a living player.
     * The mediator kills the player and logs the event.
     */
    void onNpcContactPlayer(ServerNpc npc, ServerPlayer player);

    /**
     * Called when a player collects a powerup.
     * The mediator applies the bonus and logs it.
     */
    void onPowerUpCollected(ServerPlayer player, ServerPowerup powerup);
}
