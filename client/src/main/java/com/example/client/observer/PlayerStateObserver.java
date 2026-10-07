package com.example.client.observer;

import com.example.client.ClientGameState;
import com.example.client.GameStateObserver;
import com.example.client.MapPanel;
import com.example.client.Player;

import java.util.function.IntSupplier;

/**
 * Keeps every drawn player in step with the server's.
 * <p>
 * The only observer that has to tell the local player from the remote ones, so
 * it is the only one given the local player's identity. That identity is
 * supplied as an {@link IntSupplier} rather than a plain int on purpose: the
 * server only hands out a player id after the join is accepted, which happens
 * after this observer is built, so reading it once at construction time would
 * pin every update to "not my player".
 */
public class PlayerStateObserver implements GameStateObserver {

    private final Player player;
    private final MapPanel mapPanel;
    private final IntSupplier localPlayerId;

    public PlayerStateObserver(Player player, MapPanel mapPanel, IntSupplier localPlayerId) {
        this.player = player;
        this.mapPanel = mapPanel;
        this.localPlayerId = localPlayerId;
    }

    @Override
    public void onGameState(ClientGameState state) {
        int localId = localPlayerId.getAsInt();

        for (ClientGameState.PlayerSnapshot snapshot : state.players()) {
            if (snapshot.id() == localId) {
                applyToLocalPlayer(snapshot);
            } else {
                applyToRemotePlayer(snapshot);
            }
        }
    }

    /**
     * The server is authoritative over the local player too, so its position,
     * speed and colour are all overwritten rather than predicted locally.
     */
    private void applyToLocalPlayer(ClientGameState.PlayerSnapshot snapshot) {
        player.id = snapshot.id();
        player.setPosition(snapshot.x(), snapshot.y());
        player.alive = snapshot.alive();
        player.moveSpeed = snapshot.moveSpeed();
        player.color = snapshot.color();
    }

    /**
     * Adds a remote player the first time it is seen, then only refreshes it.
     * Adding unconditionally would replace the drawn object every tick and lose
     * whatever state it had built up.
     */
    private void applyToRemotePlayer(ClientGameState.PlayerSnapshot snapshot) {
        if (!mapPanel.remotePlayers.containsKey(snapshot.id())) {
            mapPanel.addRemotePlayer(snapshot.id(), snapshot.name(), snapshot.color());
        }
        mapPanel.updateRemotePlayer(snapshot.id(), snapshot.x(), snapshot.y(), snapshot.alive());
    }

}