package com.example.server.model;

public class ExtraBombPowerUp extends ServerPowerup {

    public ExtraBombPowerUp(int tileX, int tileY) {
        super(Kind.EXTRA_BOMB, tileX, tileY);
    }

    @Override
    public boolean applyTo(ServerPlayer player) {
        player.maxBombs += Kind.EXTRA_BOMB_STEP;
        return true;
    }
}
