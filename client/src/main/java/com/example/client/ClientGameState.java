package com.example.client;

import java.awt.Color;
import java.util.List;

/**
 * One server update, already translated into the shapes the client works with.
 * <p>
 * Immutable, and built once per broadcast by {@link GameStateSubject} before
 * anybody is notified. That is what keeps the fan-out cheap: without it each
 * observer would re-parse the same tile ordinals and hex colours for its own
 * slice of the same message.
 * <p>
 * Immutable also means an observer cannot disturb the update the observer after
 * it is about to see, which is the failure mode to guard against once the
 * subject is handing the same object to several listeners.
 *
 * @param map            the authoritative tile layout
 * @param serverBombs    bombs the server has placed, as tile coordinates
 * @param explosionTiles one tile list per live explosion
 * @param players        every player the server knows about, local one included
 * @param npcs           the NPCs the server currently simulates
 * @param powerups       the powerups currently lying on the map
 */
public record ClientGameState(
        TileType[][] map,
        List<int[]> serverBombs,
        List<int[][]> explosionTiles,
        List<PlayerSnapshot> players,
        List<NPC> npcs,
        List<Powerup> powerups) {

    /**
     * One player as the client needs them. The colour arrives parsed, so no
     * observer has to know the wire format for colours.
     *
     * @param moveSpeed the speed the server last simulated for this player,
     *                  which already reflects any powerup bonus. The client
     *                  never decides it.
     */
    public record PlayerSnapshot(
            int id, String name, int x, int y, boolean alive, Color color, int moveSpeed) {}
}