package com.example.server.model;

public class FasterPowerUp extends ServerPowerup {

    public FasterPowerUp(int tileX, int tileY) {
        super(Kind.FASTER, tileX, tileY);
    }

    @Override
    public boolean applyTo(ServerPlayer player) {
        player.moveSpeed += Kind.FASTER_STEP;
        return true;
    }
}
