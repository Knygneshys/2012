package com.example.server.model.decorator;

import com.example.server.model.IPowerUp;
import com.example.server.model.ServerPlayer;

/**
 * Base decorator for IPowerUp implementations.
 */
public abstract class PowerUpDecorator implements IPowerUp {
    protected final IPowerUp delegate;

    protected PowerUpDecorator(IPowerUp delegate) {
        this.delegate = delegate;
    }

    @Override
    public boolean applyTo(ServerPlayer player) {
        return delegate.applyTo(player);
    }

    @Override
    public int tileX() {
        return delegate.tileX();
    }

    @Override
    public int tileY() {
        return delegate.tileY();
    }

    @Override
    public int getKindId() {
        return delegate.getKindId();
    }
}

