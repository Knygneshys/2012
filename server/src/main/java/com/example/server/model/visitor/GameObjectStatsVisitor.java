package com.example.server.model.visitor;

import com.example.server.model.ServerBomb;
import com.example.server.model.ServerNpc;
import com.example.server.model.ServerPlayer;
import com.example.server.model.ServerPowerup;

import java.util.ArrayList;
import java.util.List;

/**
 * Visitor that collects a human-readable status summary of every game object.
 * Used by the console interpreter's "status" command and the admin REST endpoint.
 * Replaces the scattered ad-hoc getPlayerSummaryList logic.
 */
public class GameObjectStatsVisitor implements GameObjectVisitor {

    private final List<String> lines = new ArrayList<>();

    @Override
    public void visit(ServerPlayer player) {
        lines.add(String.format("[Player #%d] name=%s pos=(%d,%d) alive=%b speed=%d maxBombs=%d radius=%d",
                player.id, player.name, player.x, player.y,
                player.alive, player.moveSpeed, player.maxBombs, player.bombRadius));
    }

    @Override
    public void visit(ServerNpc npc) {
        lines.add(String.format("[NPC    #%d] pos=(%d,%d) alive=%b speed=%d behaviour=%s",
                npc.id, npc.x, npc.y, npc.alive, npc.moveSpeed, npc.behaviour()));
    }

    @Override
    public void visit(ServerBomb bomb) {
        lines.add(String.format("[Bomb      ] tile=(%d,%d) fuse=%dms radius=%d owner=#%d",
                bomb.tileX, bomb.tileY, bomb.remainingMs, bomb.radius, bomb.ownerId));
    }

    @Override
    public void visit(ServerPowerup powerup) {
        lines.add(String.format("[PowerUp   ] kind=%s tile=(%d,%d)",
                powerup.kind, powerup.tileX, powerup.tileY));
    }

    @Override
    public void visit(com.example.server.model.blocks.ServerBlock block) {
        // Blocks generally not tracked individually in stats to avoid 200+ lines, but supported
    }

    /** Returns the collected lines (one per visited object). */
    public List<String> getLines() {
        return List.copyOf(lines);
    }

    /** Clears collected lines so the visitor can be reused. */
    public void reset() {
        lines.clear();
    }
}
