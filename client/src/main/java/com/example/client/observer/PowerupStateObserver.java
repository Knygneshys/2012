package com.example.client.observer;

import com.example.client.ClientGameState;
import com.example.client.GameStateObserver;
import com.example.client.MapPanel;

/**
 * Keeps the visible powerups in step with the server's.
 * <p>
 * Sees the powerup list and nothing else. Split out because it is the one slice
 * with rules of its own — the client applies a collected bonus locally rather
 * than waiting for the next broadcast — and that rule had no business sitting
 * in the same method as terrain and NPC handling.
 */
public class PowerupStateObserver implements GameStateObserver {

    private final MapPanel mapPanel;

    public PowerupStateObserver(MapPanel mapPanel) {
        this.mapPanel = mapPanel;
    }

    @Override
    public void onGameState(ClientGameState state) {
        mapPanel.updatePowerups(state.powerups());
    }

}