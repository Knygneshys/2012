package com.example.server.model;

import com.example.server.mediator.GameEventMediator;
import com.example.server.model.ServerPlayer;

import java.util.Random;

/**
 * Server side counterpart of the client Powerup. Drops on the tile where a
 * breakable wall was destroyed and is consumed by the first player that walks
 * over it.
 */
public abstract class ServerPowerup extends ServerGameObject implements  IPowerUp{
    /**
     * The bonuses a powerup grants. The ordinal of each constant is what gets
     * sent over the network, so the order must match the client Powerup.Kind.
     */
    public enum Kind {
        EXTRA_BOMB,
        BIGGER_BOMB,
        FASTER;

        public static final int EXTRA_BOMB_STEP = 1;
        public static final int BIGGER_BOMB_STEP = 1;
        public static final int FASTER_STEP = 2;

        public static Kind random(Random random) {
            Kind[] values = values();
            return values[random.nextInt(values.length)];
        }
    }

    public final Kind kind;
    public final int tileX;
    public final int tileY;

    private GameEventMediator mediator;

    public ServerPowerup(Kind kind, int tileX, int tileY) {
        this.kind = kind;
        this.tileX = tileX;
        this.tileY = tileY;
    }

    public void setMediator(GameEventMediator mediator) {
        this.mediator = mediator;
    }

    /**
     * Notifies the mediator that this powerup was collected by a player.
     */
    public void collectedBy(ServerPlayer player) {
        if (mediator != null) {
            mediator.onPowerUpCollected(player, this);
        }
    }

    /**
     * Applies this powerup's bonus to a player.
     */
    @Override
    public abstract boolean applyTo(ServerPlayer player);

    @Override
    public int tileX() {
        return tileX;
    }

    @Override
    public int tileY() {
        return tileY;
    }

    @Override
    public int getKindId() {
        return kind.ordinal();
    }

    @Override
    public void accept(com.example.server.model.visitor.GameObjectVisitor visitor) {
        visitor.visit(this);
    }
}