package com.example.client.observer;

import com.example.client.ClientGameState;
import com.example.client.GameStateObserver;
import com.example.client.MapPanel;

/**
 * Keeps the rendered terrain in step with the server's.
 * <p>
 * One of four observers on the same broadcast. It is told about the map, the
 * explosions and the server's bombs, and about nothing else — it never learns
 * that NPCs or powerups exist, which is what it would have to know about if the
 * single handler still existed.
 */
public class MapStateObserver implements GameStateObserver {

    private final MapPanel mapPanel;

    public MapStateObserver(MapPanel mapPanel) {
        this.mapPanel = mapPanel;
    }

    @Override
    public void onGameState(ClientGameState state) {
        mapPanel.updateGameState(state.map(), state.explosionTiles(), state.serverBombs());
        mapPanel.repaint();
    }

}