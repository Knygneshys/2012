
package com.example.server.model;

import com.example.server.GameConstants;
import com.example.server.model.blocks.ServerBlock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

public class Portal {

    private final ServerBlock[][] map;
    private final Collection<ServerPlayer> players;
    private final Collection<ServerNpc> npcs;
    private final Collection<ServerBomb> bombs;
    private final Collection<ServerExplosion> explosions;
    private final Random random;

    public Portal(
            ServerBlock[][] map,
            Collection<ServerPlayer> players,
            Collection<ServerNpc> npcs,
            Collection<ServerBomb> bombs,
            Collection<ServerExplosion> explosions,
            Random random) {

        this.map = map;
        this.players = players;
        this.npcs = npcs;
        this.bombs = bombs;
        this.explosions = explosions;
        this.random = random;
    }

    public boolean teleport(ServerPlayer player) {
        if (!player.alive) return false;

        List<int[]> freeTiles = findFreeTiles(player);

        if (freeTiles.isEmpty()) return false;

        int[] destination = freeTiles.get(
                random.nextInt(freeTiles.size())
        );

        player.x = destination[0] * GameConstants.TILE_SIZE;
        player.y = destination[1] * GameConstants.TILE_SIZE;

        return true;
    }

    public int getAvailableDestinations(ServerPlayer player) {
        return findFreeTiles(player).size();
    }

    private List<int[]> findFreeTiles(ServerPlayer player) {
        List<int[]> freeTiles = new ArrayList<>();

        for (int y = 0; y < map.length; y++) {
            for (int x = 0; x < map[y].length; x++) {

                if (isSafeTile(x, y, player)) {
                    freeTiles.add(new int[]{x, y});
                }
            }
        }

        return freeTiles;
    }

    private boolean isSafeTile(
            int x, int y, ServerPlayer player) {

        if (!map[y][x].isPassable()) return false;

        // Do not teleport onto the player's current position
        if (player.overlapsTile(x, y)) return false;

        // Avoid bombs
        for (ServerBomb bomb : bombs) {
            if (bomb.tileX == x && bomb.tileY == y)
                return false;
        }

        // Avoid active explosions
        for (ServerExplosion explosion : explosions) {
            for (int[] tile : explosion.tiles) {
                if (tile[0] == x && tile[1] == y)
                    return false;
            }
        }

        // Avoid other living players
        for (ServerPlayer other : players) {
            if (other != player && other.alive &&
                    other.overlapsTile(x, y))
                return false;
        }

        // Avoid living NPCs
        for (ServerNpc npc : npcs) {
            if (npc.alive && npc.overlapsTile(x, y))
                return false;
        }

        return true;
    }
}
