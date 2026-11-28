package com.example.server.model.visitor;

import com.example.server.model.ServerBomb;
import com.example.server.model.ServerNpc;
import com.example.server.model.ServerPlayer;
import com.example.server.model.ServerPowerup;
import com.example.server.network.GameStateData;

/**
 * Visitor that serialises every game object into the {@link GameStateData}
 * wire format sent to connected clients each tick.
 *
 * Extracts the DTO-building that was previously inlined in GameServer.broadcastGameState().
 */
public class SerializationVisitor implements GameObjectVisitor {

    private final GameStateData data;

    public SerializationVisitor(GameStateData data) {
        this.data = data;
    }

    @Override
    public void visit(ServerPlayer player) {
        GameStateData.PlayerData pd = new GameStateData.PlayerData(
                player.id, player.name, player.x, player.y,
                player.alive, player.colorHex, player.moveSpeed);
        pd.maxBombs = player.maxBombs;
        pd.bombRadius = player.bombRadius;
        data.players.add(pd);
    }

    @Override
    public void visit(ServerNpc npc) {
        data.npcs.add(new GameStateData.NpcData(
                npc.id, npc.x, npc.y, npc.moveSpeed, npc.alive, npc.colorHex));
    }

    @Override
    public void visit(ServerBomb bomb) {
        data.bombs.add(new GameStateData.BombData(
                bomb.tileX, bomb.tileY, bomb.remainingMs, bomb.radius));
    }

    @Override
    public void visit(ServerPowerup powerup) {
        data.powerups.add(new GameStateData.PowerupData(
                powerup.kind.ordinal(), powerup.tileX, powerup.tileY));
    }

    @Override
    public void visit(com.example.server.model.blocks.ServerBlock block) {
        if (data.map != null && block.tileY() < data.map.length && block.tileX() < data.map[0].length) {
            data.map[block.tileY()][block.tileX()] = block.tileType().ordinal();
        }
    }
}
