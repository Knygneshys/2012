
package com.example.server.model;

public class PortalAdapter implements IPowerUp {

    // The first three IDs belong to the existing power-ups (0, 1, 2)
    public static final int PORTAL_KIND_ID = 3;

    private final Portal portal;
    private final int tileX;
    private final int tileY;

    public PortalAdapter(Portal portal, int tileX, int tileY) {
        this.portal = portal;
        this.tileX = tileX;
        this.tileY = tileY;
    }

    @Override
    public boolean applyTo(ServerPlayer player) {
        return portal.teleport(player);
    }

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
        return PORTAL_KIND_ID;
    }
}
