package com.example.server.mediator;

import com.example.server.GameLogger;
import com.example.server.model.ServerBomb;
import com.example.server.model.ServerExplosion;
import com.example.server.model.ServerNpc;
import com.example.server.model.ServerPlayer;
import com.example.server.model.ServerPowerup;
import com.example.server.model.blocks.ServerBlock;

import java.util.List;
import java.util.Map;

/**
 * Concrete Mediator.
 *
 * <p>Coordinates events between the three primary colleagues:
 * <ul>
 *   <li>{@link ServerPlayer} – killed by explosion or NPC
 *   <li>{@link ServerBomb}   – detonates; mediator triggers damage to players/NPCs
 *   <li>{@link ServerNpc}    – contacts player; mediator kills the player
 * </ul>
 * and the supporting colleague {@link ServerPowerup} (collected by a player).
 *
 * <p><b>Mediator vs Observer:</b> The client-side {@code GameStateSubject} (Observer)
 * simply notifies its listeners with a snapshot; the listeners decide independently
 * what to do. This mediator is the opposite: it owns the game-rule knowledge and
 * tells colleagues what to do, so none of them need to know about each other.
 */
public class GameRoundMediator implements GameEventMediator {

    private static final GameLogger log = GameLogger.getInstance();

    private final Map<Integer, ServerPlayer> players;
    private final List<ServerNpc> npcs;
    private final List<ServerBomb> bombs;
    private final List<ServerExplosion> explosions;
    private final ServerBlock[][] map;

    public GameRoundMediator(
            Map<Integer, ServerPlayer> players,
            List<ServerNpc> npcs,
            List<ServerBomb> bombs,
            List<ServerExplosion> explosions,
            ServerBlock[][] map) {
        this.players    = players;
        this.npcs       = npcs;
        this.bombs      = bombs;
        this.explosions = explosions;
        this.map        = map;
    }

    // -------------------------------------------------------------------------
    // GameEventMediator implementation
    // -------------------------------------------------------------------------

    @Override
    public void onBombExploded(ServerBomb bomb) {
        log.info("[Mediator] Bomb at (" + bomb.tileX + "," + bomb.tileY + ") exploded.");
        ServerExplosion ex = buildExplosion(bomb);
        explosions.add(ex);
        applyExplosionDamage(ex);
        checkRoundState("explosion");
    }

    @Override
    public void onPlayerKilled(ServerPlayer player, String cause) {
        log.info("[Mediator] Player " + player.name + " killed by " + cause + ".");
        checkRoundState(cause);
    }

    @Override
    public void onNpcContactPlayer(ServerNpc npc, ServerPlayer player) {
        if (!player.alive) return;
        player.kill();
        log.info("[Mediator] Player " + player.name + " caught by NPC #" + npc.id + "!");
        checkRoundState("NPC contact");
    }

    @Override
    public void onPowerUpCollected(ServerPlayer player, ServerPowerup powerup) {
        log.info("[Mediator] Player " + player.name
                + " collected " + powerup.kind + " at ("
                + powerup.tileX + "," + powerup.tileY + ").");
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Builds the blast shape for a detonated bomb, updating the map when
     * destructible walls are hit, and returns the resulting explosion.
     */
    private ServerExplosion buildExplosion(ServerBomb bomb) {
        ServerExplosion ex = new ServerExplosion();
        int bx = bomb.tileX;
        int by = bomb.tileY;
        ex.addTile(bx, by);

        int[][] dirs = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        for (int[] d : dirs) {
            for (int step = 1; step <= bomb.radius; step++) {
                int nx = bx + d[0] * step;
                int ny = by + d[1] * step;
                if (nx < 0 || ny < 0 || ny >= map.length || nx >= map[0].length) break;
                ServerBlock block = map[ny][nx];
                if (block.isDestructible()) {
                    ex.addTile(nx, ny);
                    map[ny][nx] = block.destroyed();
                    break;
                }
                if (block.stopsBlast()) break;
                ex.addTile(nx, ny);
            }
        }
        return ex;
    }

    /**
     * Kills any player or NPC overlapping a blast tile.
     * Also flags bombs caught in the blast for chain detonation.
     */
    public void applyExplosionDamage(ServerExplosion ex) {
        java.util.List<ServerBomb> chainDetonations = new java.util.ArrayList<>();
        com.example.server.model.visitor.ExplosionDamageVisitor damageVisitor =
                new com.example.server.model.visitor.ExplosionDamageVisitor(java.util.List.of(ex), chainDetonations);

        for (ServerPlayer p : players.values()) {
            p.accept(damageVisitor);
        }
        for (ServerNpc npc : npcs) {
            npc.accept(damageVisitor);
        }
        for (ServerBomb bomb : bombs) {
            bomb.accept(damageVisitor);
        }
    }

    /**
     * Checks whether the round has ended:
     * - all players dead → round over
     * - all NPCs dead    → players win
     */
    private void checkRoundState(String trigger) {
        long alivePlayers = players.values().stream().filter(p -> p.alive).count();
        long aliveNpcs    = npcs.stream().filter(n -> n.alive).count();

        if (alivePlayers == 0) {
            log.info("[Mediator] Round over — no living players remain (triggered by: " + trigger + ").");
        } else if (aliveNpcs == 0) {
            log.info("[Mediator] Players win! All NPCs eliminated (triggered by: " + trigger + ").");
        }
    }
}
