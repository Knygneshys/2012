package com.example.server.model;

import com.example.server.mediator.GameEventMediator;

public class ServerBomb extends ServerGameObject {
    public int tileX;
    public int tileY;
    public int remainingMs;
    public int radius;
    /**
     * Id of the player that placed the bomb, used to enforce their bomb limit.
     */
    public int ownerId = -1;

    private GameEventMediator mediator;

    public ServerBomb(int tileX, int tileY, int fuseMs, int radius) {
        this(tileX, tileY, fuseMs, radius, -1);
    }

    public ServerBomb(int tileX, int tileY, int fuseMs, int radius, int ownerId) {
        this.tileX = tileX;
        this.tileY = tileY;
        this.remainingMs = fuseMs;
        this.radius = radius;
        this.ownerId = ownerId;
    }

    public void setMediator(GameEventMediator mediator) {
        this.mediator = mediator;
    }

    /**
     * Detonates the bomb through the mediator instead of letting GameServer
     * handle the blast directly.
     */
    public void explode() {
        if (mediator != null) {
            mediator.onBombExploded(this);
        }
    }

    @Override
    public int tileX() {
        return tileX;
    }

    @Override
    public int tileY() {
        return tileY;
    }

    public boolean tick(int deltaMs) {
        remainingMs -= deltaMs;
        return remainingMs <= 0;
    }

    @Override
    public void accept(com.example.server.model.visitor.GameObjectVisitor visitor) {
        visitor.visit(this);
    }
}