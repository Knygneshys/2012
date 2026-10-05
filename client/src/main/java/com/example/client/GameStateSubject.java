package com.example.client;

import com.example.client.network.GameStateData;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The subject: holds the observers and tells all of them when an update lands.
 * <p>
 * Every broadcast is translated into a {@link ClientGameState} once, here, and
 * the same immutable snapshot is then handed to each observer in turn. Doing
 * the translation at the subject rather than in the observers is what keeps the
 * cost flat as observers are added — a fifth observer costs one more update
 * call, not another pass over the tile grid.
 * <p>
 * The subscriber list is a {@link CopyOnWriteArrayList} because updates arrive
 * on the socket thread while a window may be closing and unsubscribing on the
 * event dispatch thread. Copying the array on write makes iteration safe
 * without a lock on the read path, which matters here: this runs ten times a
 * second and must never block the game loop's paint.
 * <p>
 * The subject stays deliberately ignorant of what its observers do with the
 * update. It knows how to turn wire data into a snapshot and how to hand it
 * out; whether the map, an NPC list or a score display cares is not its
 * business. That is the coupling it exists to remove.
 */
public class GameStateSubject {

    private final List<GameStateObserver> observers = new CopyOnWriteArrayList<>();

    /**
     * Registers an observer. Registering the same observer twice would have it
     * updated twice per broadcast, so repeats are ignored.
     */
    public void subscribe(GameStateObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    public void unsubscribe(GameStateObserver observer) {
        observers.remove(observer);
    }

    public int observerCount() {
        return observers.size();
    }

    /**
     * Translates a server update and notifies every observer.
     * <p>
     * Called on the event dispatch thread, so observers may touch Swing
     * components directly.
     */
    public void publish(GameStateData data) {
        ClientGameState state = translate(data);
        for (GameStateObserver observer : observers) {
            observer.onGameState(state);
        }
    }

    /**
     * Turns the wire format into the client's own objects.
     * <p>
     * The map arrives as {@code TileType} ordinals and colours as hex strings,
     * both of which are protocol details. Resolving them once, here, is what
     * lets the observers be written purely in terms of game objects.
     */
    private ClientGameState translate(GameStateData data) {
        return new ClientGameState(
                translateMap(data.map),
                translateBombs(data.bombs),
                translateExplosions(data.explosions),
                translatePlayers(data.players),
                translateNpcs(data.npcs),
                translatePowerups(data.powerups));
    }

    private TileType[][] translateMap(int[][] raw) {
        TileType[] types = TileType.values();
        TileType[][] map = new TileType[raw.length][];
        for (int y = 0; y < raw.length; y++) {
            map[y] = new TileType[raw[y].length];
            for (int x = 0; x < raw[y].length; x++) {
                map[y][x] = types[raw[y][x]];
            }
        }
        return map;
    }

    private List<int[]> translateBombs(List<GameStateData.BombData> bombs) {
        List<int[]> tiles = new ArrayList<>(bombs.size());
        for (GameStateData.BombData bomb : bombs) {
            tiles.add(new int[]{bomb.tileX, bomb.tileY});
        }
        return tiles;
    }

    private List<int[][]> translateExplosions(List<GameStateData.ExplosionData> explosions) {
        List<int[][]> tiles = new ArrayList<>(explosions.size());
        for (GameStateData.ExplosionData explosion : explosions) {
            tiles.add(explosion.tiles);
        }
        return tiles;
    }

    private List<ClientGameState.PlayerSnapshot> translatePlayers(List<GameStateData.PlayerData> players) {
        List<ClientGameState.PlayerSnapshot> snapshots = new ArrayList<>(players.size());
        for (GameStateData.PlayerData player : players) {
            snapshots.add(new ClientGameState.PlayerSnapshot(
                    player.playerId, player.name, player.x, player.y,
                    player.alive, hexToColor(player.colorHex), player.moveSpeed));
        }
        return snapshots;
    }

    private List<NPC> translateNpcs(List<GameStateData.NpcData> npcs) {
        List<NPC> built = new ArrayList<>(npcs.size());
        for (GameStateData.NpcData npc : npcs) {
            // alive and moveSpeed come from the server, the client never
            // decides them itself
            built.add(new NPC(npc.npcId, "NPC " + npc.npcId, npc.x, npc.y,
                    npc.moveSpeed, hexToColor(npc.colorHex),
                    MapPanel.TILE_SIZE, MapPanel.TILE_SIZE, npc.alive));
        }
        return built;
    }

    private List<Powerup> translatePowerups(List<GameStateData.PowerupData> powerups) {
        List<Powerup> built = new ArrayList<>(powerups.size());
        for (GameStateData.PowerupData powerup : powerups) {
            built.add(new Powerup(Powerup.Kind.fromOrdinal(powerup.kind),
                    powerup.tileX, powerup.tileY, MapPanel.TILE_SIZE));
        }
        return built;
    }

    private static Color hexToColor(String hex) {
        String digits = hex.startsWith("#") ? hex.substring(1) : hex;
        int r = Integer.parseInt(digits.substring(0, 2), 16);
        int g = Integer.parseInt(digits.substring(2, 4), 16);
        int b = Integer.parseInt(digits.substring(4, 6), 16);
        return new Color(r, g, b);
    }
}