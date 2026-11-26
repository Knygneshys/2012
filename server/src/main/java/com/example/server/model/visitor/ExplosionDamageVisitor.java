package com.example.server.model.visitor;

import com.example.server.GameLogger;
import com.example.server.model.ServerBomb;
import com.example.server.model.ServerExplosion;
import com.example.server.model.ServerNpc;
import com.example.server.model.ServerPlayer;
import com.example.server.model.ServerPowerup;

import java.util.List;

/**
 * Visitor that applies explosion blast damage to every game object it visits.
 * Characters overlapping any blast tile are killed; bombs caught in the blast
 * are marked for chain-detonation; powerups on a blast tile are destroyed.
 *
 * Replaces the inline applyBlast loops previously spread across GameServer.
 */
public class ExplosionDamageVisitor implements GameObjectVisitor {

    private static final GameLogger log = GameLogger.getInstance();

    private final List<ServerExplosion> explosions;
    /** Bombs that should chain-detonate because a blast reached them. */
    private final List<ServerBomb> chainDetonations;

    public ExplosionDamageVisitor(List<ServerExplosion> explosions, List<ServerBomb> chainDetonations) {
        this.explosions = explosions;
        this.chainDetonations = chainDetonations;
    }

    @Override
    public void visit(ServerPlayer player) {
        if (!player.alive) return;
        for (ServerExplosion ex : explosions) {
            for (int[] tile : ex.tiles) {
                if (player.overlapsTile(tile[0], tile[1])) {
                    player.killedBy("explosion");
                    log.info("Player " + player.name + " eliminated by explosion!");
                    return;
                }
            }
        }
    }

    @Override
    public void visit(ServerNpc npc) {
        if (!npc.alive) return;
        for (ServerExplosion ex : explosions) {
            for (int[] tile : ex.tiles) {
                if (npc.overlapsTile(tile[0], tile[1])) {
                    npc.kill();
                    log.info("NPC #" + npc.id + " eliminated by explosion!");
                    return;
                }
            }
        }
    }

    @Override
    public void visit(ServerBomb bomb) {
        // A bomb caught in an explosion chain-detonates immediately
        for (ServerExplosion ex : explosions) {
            for (int[] tile : ex.tiles) {
                if (bomb.tileX == tile[0] && bomb.tileY == tile[1]) {
                    if (!chainDetonations.contains(bomb)) {
                        chainDetonations.add(bomb);
                    }
                    return;
                }
            }
        }
    }

    @Override
    public void visit(ServerPowerup powerup) {
        // powerups caught in blast are silently removed — caller removes them
        // from the PowerUpHandler after visiting
    }

    @Override
    public void visit(com.example.server.model.blocks.ServerBlock block) {
        // Block destruction handled during explosion raycast
    }
}
