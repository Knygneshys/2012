package com.example.client.observer;

import com.example.client.ClientGameState;
import com.example.client.GameStateObserver;
import com.example.client.HudBar;
import com.example.client.MapPanel;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntSupplier;

/**
 * Feeds the hotbar with the local player's stats and tells the map whether to
 * draw the lobby overlay. A fifth observer on the same broadcast.
 */
public class HudStateObserver implements GameStateObserver {

    private final HudBar hud;
    private final MapPanel mapPanel;
    private final IntSupplier localPlayerId;
    private final IntSupplier hostPlayerId;

    public HudStateObserver(HudBar hud, MapPanel mapPanel, IntSupplier localPlayerId, IntSupplier hostPlayerId) {
        this.hud = hud;
        this.mapPanel = mapPanel;
        this.localPlayerId = localPlayerId;
        this.hostPlayerId = hostPlayerId;
    }

    @Override
    public void onGameState(ClientGameState state) {
        int localId = localPlayerId.getAsInt();
        int hostId = hostPlayerId.getAsInt();

        ClientGameState.PlayerSnapshot me = null;
        List<String> lobbyLabels = new ArrayList<>();
        for (ClientGameState.PlayerSnapshot p : state.players()) {
            if (p.id() == localId) me = p;
            String label = (p.id() == hostId ? "★ " : "") + p.name() + (p.id() == localId ? " (you)" : "");
            lobbyLabels.add(label);
        }

        hud.update(me, localId != -1 && localId == hostId, state.lobby());
        mapPanel.setLobby(state.lobby(), lobbyLabels);
    }
}
