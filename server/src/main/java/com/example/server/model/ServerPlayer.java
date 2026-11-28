package com.example.server.model;

import com.example.server.GameConstants;
import com.example.server.mediator.GameEventMediator;
import com.example.server.model.blocks.ServerBlock;

/**
 * A connected player. Position, speed and the movement rule come from
 * {@link ServerCharacter}; what is left here is the player's own stats and the
 * reset back to its spawn.
 */
public class ServerPlayer extends ServerCharacter {
    public String name;
    public String colorHex;
    /**
     * How many bombs this player may have on the map at the same time.
     */
    public int maxBombs = GameConstants.PLAYER_MAX_BOMBS;
    /**
     * How far this player's bombs spread. Both are raised by powerups.
     */
    public int bombRadius = GameConstants.BOMB_RADIUS;

    private final int spawnX;
    private final int spawnY;

    public ServerPlayer(int id, String name, int x, int y, String colorHex) {
        super(id, x, y, GameConstants.PLAYER_MOVE_SPEED);
        this.name = name;
        this.colorHex = colorHex;
        this.spawnX = x;
        this.spawnY = y;
    }

    public void reset() {
        x = spawnX;
        y = spawnY;
        alive = true;
        moveSpeed = GameConstants.PLAYER_MOVE_SPEED;
        maxBombs = GameConstants.PLAYER_MAX_BOMBS;
        bombRadius = GameConstants.BOMB_RADIUS;
    }

    /**
     * Player movement, more forgiving than the strict {@link #move} NPCs use:
     * <ol>
     *   <li>If the full step is blocked, take the largest shorter step that
     *       fits, so the player ends flush against the wall and lined up with
     *       the lanes instead of stopping a few pixels short.</li>
     *   <li>If no step fits at all, try <b>corner assist</b>: when the player is
     *       up to {@link GameConstants#CORNER_ASSIST_PX} off a lane that is open
     *       in the direction pressed, slide them towards that lane. Holding a
     *       direction then rounds the corner on its own.</li>
     * </ol>
     */
    public boolean moveAssisted(int dx, int dy, ServerBlock[][] map) {
        if (!alive || (dx == 0 && dy == 0) || map == null || map.length == 0) return false;
        if (move(dx, dy, map)) return true;

        // 1. shorter step
        int step = Math.abs(dx != 0 ? dx : dy);
        int sx = Integer.signum(dx);
        int sy = Integer.signum(dy);
        for (int s = step - 1; s > 0; s--) {
            if (move(sx * s, sy * s, map)) return true;
        }

        // 2. corner assist along the perpendicular axis
        int size = GameConstants.TILE_SIZE;
        int perpendicular = dx != 0 ? y : x;
        int offset = Math.floorMod(perpendicular, size);
        if (offset == 0) return false;
        // Try the nearer lane first, then the farther one.
        int towardLower = -offset;
        int towardUpper = size - offset;
        int[] candidates = Math.abs(towardLower) <= towardUpper
                ? new int[]{towardLower, towardUpper}
                : new int[]{towardUpper, towardLower};
        for (int shift : candidates) {
            if (Math.abs(shift) > GameConstants.CORNER_ASSIST_PX) continue;
            int laneX = dx != 0 ? x : x + shift;
            int laneY = dx != 0 ? y + shift : y;
            // the lane must be reachable and open one pixel further in the pressed direction
            if (!canOccupy(laneX, laneY, map) || !canOccupy(laneX + sx, laneY + sy, map)) continue;
            int nudge = Integer.signum(shift) * Math.min(Math.abs(shift), moveSpeed);
            return dx != 0 ? move(0, nudge, map) : move(nudge, 0, map);
        }
        return false;
    }

    @Override
    public void accept(com.example.server.model.visitor.GameObjectVisitor visitor) {
        visitor.visit(this);
    }

    /**
     * Kills this player and notifies the mediator so it can log the death and
     * check round end conditions.
     */
    public void killedBy(String cause) {
        kill();
        GameEventMediator mediator = getMediator();
        if (mediator != null) {
            mediator.onPlayerKilled(this, cause);
        }
    }
}