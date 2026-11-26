package com.example.server.interpreter;

import com.example.server.GameLogger;

/**
 * Terminal expression: prints available console commands.
 * Grammar: "help"
 */
public class HelpExpression implements ConsoleExpression {

    private static final GameLogger log = GameLogger.getInstance();

    @Override
    public void interpret(ConsoleContext ctx) {
        log.info("Available console commands:");
        log.info("  status   - show server status (players, port, running)");
        log.info("  players  - list all connected players");
        log.info("  bombs    - count active bombs on the map");
        log.info("  npcs     - show NPC summary");
        log.info("  reset    - reset the game to initial state");
        log.info("  help     - show this help message");
    }
}
