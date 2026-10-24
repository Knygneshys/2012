package com.example.server.model.decorator;

import com.example.server.GameLogger;
import com.example.server.model.IPowerUp;
import com.example.server.model.ServerPlayer;

/**
 * Shields the game loop from power-up implementation failures.
 */
public class SafeApplyPowerUpDecorator extends PowerUpDecorator {
    private static final GameLogger log = GameLogger.getInstance();

    public SafeApplyPowerUpDecorator(IPowerUp delegate) {
        super(delegate);
    }

    @Override
    public boolean applyTo(ServerPlayer player) {
        try {
            return super.applyTo(player);
        } catch (RuntimeException e) {
            log.error("Power-up " + getKindId() + " failed at " + tileX() + "," + tileY(), e);
            return false;
        }
    }
}

