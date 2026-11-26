package com.example.server.interpreter;

import com.example.server.GameLogger;

import java.util.List;
import java.util.Map;

/**
 * Terminal expression: lists all connected players with their position and alive status.
 * Grammar: "players"
 */
public class PlayersExpression implements ConsoleExpression {

    private static final GameLogger log = GameLogger.getInstance();

    @Override
    public void interpret(ConsoleContext ctx) {
        List<Map<String, Object>> players = ctx.server.getPlayerSummaryList();
        if (players.isEmpty()) {
            log.info("[Players] No players connected.");
            return;
        }
        log.info("=== Connected Players ===");
        for (Map<String, Object> p : players) {
            log.info(String.format("  #%-2s %-12s pos=(%s,%s) alive=%s color=%s",
                    p.get("id"), p.get("name"), p.get("x"), p.get("y"),
                    p.get("alive"), p.get("color")));
        }
        log.info("=========================");
    }
}
