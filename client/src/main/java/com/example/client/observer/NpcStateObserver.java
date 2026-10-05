package com.example.client.observer;

import com.example.client.ClientGameState;
import com.example.client.GameStateObserver;
import com.example.client.MapPanel;

/**
 * Keeps the displayed NPCs in step with the server's.
 * <p>
 * Sees the NPC list and nothing else. {@link MapPanel#updateNpcs} keeps the same
 * object for an NPC that is still present and refreshes it, so the drawn
 * entities stay identical across ticks.
 */
public class NpcStateObserver implements GameStateObserver {

    private final MapPanel mapPanel;

    public NpcStateObserver(MapPanel mapPanel) {
        this.mapPanel = mapPanel;
    }

    @Override
    public void onGameState(ClientGameState state) {
        mapPanel.updateNpcs(state.npcs());
    }

}