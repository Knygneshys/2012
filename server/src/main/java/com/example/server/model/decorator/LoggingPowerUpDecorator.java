package com.example.server.model.decorator;

import com.example.server.GameLogger;
import com.example.server.model.IPowerUp;
import com.example.server.model.ServerPlayer;

/**
 * Adds collection logs around an existing power-up without changing its core behavior.
 */
public class LoggingPowerUpDecorator extends PowerUpDecorator {
    private static final GameLogger log = GameLogger.getInstance();

    public LoggingPowerUpDecorator(IPowerUp delegate) {
        super(delegate);
    }

    @Override
    public boolean applyTo(ServerPlayer player) {
        log.info("Player " + player.name + " triggered power-up " + getKindId()
                + " at " + tileX() + "," + tileY());

        boolean consumed = super.applyTo(player);

        if (consumed) {
            log.info("Power-up " + getKindId() + " consumed by " + player.name);
        } else {
            log.info("Power-up " + getKindId() + " was not consumed by " + player.name);
        }

        return consumed;
    }
}

