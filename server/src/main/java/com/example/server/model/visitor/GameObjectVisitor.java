package com.example.server.model.visitor;

import com.example.server.model.ServerBomb;
import com.example.server.model.ServerNpc;
import com.example.server.model.ServerPlayer;
import com.example.server.model.ServerPowerup;

/**
 * Visitor pattern: defines an operation over the ServerGameObject hierarchy
 * without changing the classes. Add a new operation by implementing this
 * interface rather than modifying every model class.
 */
public interface GameObjectVisitor {
    void visit(ServerPlayer player);
    void visit(ServerNpc npc);
    void visit(ServerBomb bomb);
    void visit(ServerPowerup powerup);
    void visit(com.example.server.model.blocks.ServerBlock block);
}
