
package com.example.server.model;

import com.example.server.mediator.GameEventMediator;
import com.example.server.model.ServerPlayer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class PowerUpHandler {

    private final List<IPowerUp> powerUps = new ArrayList<>();
    private GameEventMediator mediator;

    public synchronized  void addPowerUp(IPowerUp powerUp) {
        powerUps.add(powerUp);
        if (powerUp instanceof ServerPowerup sp) {
            sp.setMediator(mediator);
        }
    }

    public void setMediator(GameEventMediator mediator) {
        this.mediator = mediator;
        for (IPowerUp powerUp : powerUps) {
            if (powerUp instanceof ServerPowerup sp) {
                sp.setMediator(mediator);
            }
        }
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
                        if (powerUp instanceof ServerPowerup sp) {
                            sp.collectedBy(player);
                        }
                        iterator.remove();
                    }
                    break;
                }
            }
        }
    }
}
