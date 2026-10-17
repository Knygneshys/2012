package com.example.server.model;

public class BiggerBombPowerUp extends ServerPowerup {

    public BiggerBombPowerUp(int tileX, int tileY) {
        super(Kind.BIGGER_BOMB, tileX, tileY);
    }

    @Override
    public boolean applyTo(ServerPlayer player) {
        player.bombRadius += Kind.BIGGER_BOMB_STEP;
        return true;
    }
}
