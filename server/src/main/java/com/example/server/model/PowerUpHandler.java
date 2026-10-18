
package com.example.server.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class PowerUpHandler {

    private final List<IPowerUp> powerUps = new ArrayList<>();

    public synchronized  void addPowerUp(IPowerUp powerUp) {
        powerUps.add(powerUp);
    }

    public synchronized  int getPowerUpCount() {
        return powerUps.size();
    }

    public synchronized  List<IPowerUp> getPowerUps() {
        return List.copyOf(powerUps);
    }

    public synchronized  void clear() {
        powerUps.clear();
    }

    public synchronized void collectPowerUps(Collection<ServerPlayer> players) {
        Iterator<IPowerUp> iterator = powerUps.iterator();

        while (iterator.hasNext()) {
            IPowerUp powerUp = iterator.next();

            for (ServerPlayer player : players) {
                if (player.alive && player.overlapsTile(powerUp.tileX(), powerUp.tileY())) {
                    if (powerUp.applyTo(player)) {
                        iterator.remove();
                    }
                    break;
                }
            }
        }
    }
}
