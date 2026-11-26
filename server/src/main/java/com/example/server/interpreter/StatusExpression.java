package com.example.server.interpreter;

import com.example.server.GameLogger;

/**
 * Terminal expression: prints the server's current status.
 * Grammar: "status"
 */
public class StatusExpression implements ConsoleExpression {

    private static final GameLogger log = GameLogger.getInstance();

    @Override
    public void interpret(ConsoleContext ctx) {
        log.info("=== Server Status ===");
        log.info("Running     : " + ctx.server.isRunning());
        log.info("Port        : " + ctx.server.getPort());
        log.info("Players     : " + ctx.server.getConnectedPlayerCount() + " / " + ctx.server.getMaxPlayers());
        java.util.List<String> stats = ctx.server.getAllGameObjectStats();
        if (!stats.isEmpty()) {
            log.info("--- Game Object Stats (Visitor) ---");
            for (String line : stats) {
                log.info("  " + line);
            }
        }
        log.info("=====================");
    }
}
