package com.example.server.interpreter;

import com.example.server.GameLogger;
import com.example.server.GameServer;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * The Interpreter: reads lines from stdin, tokenises them, looks up the
 * matching {@link ConsoleExpression}, and calls interpret().
 *
 * Run as a background daemon thread from {@link GameServer#run(String...)}.
 * Unknown commands are reported to the user without crashing.
 *
 * Grammar (EBNF):
 *   line       ::= command
 *   command    ::= "status" | "players" | "bombs" | "npcs" | "reset" | "help"
 */
public class ConsoleInterpreter implements Runnable {

    private static final GameLogger log = GameLogger.getInstance();

    private final GameServer server;
    private final Map<String, ConsoleExpression> expressions = new HashMap<>();

    public ConsoleInterpreter(GameServer server) {
        this.server = server;
        // Register all terminal expressions
        expressions.put("status",  new StatusExpression());
        expressions.put("players", new PlayersExpression());
        expressions.put("bombs",   new BombsExpression());
        expressions.put("npcs",    new NpcsExpression());
        expressions.put("reset",   new ResetExpression());
        expressions.put("help",    new HelpExpression());
    }

    @Override
    public void run() {
        log.info("[Console] Interpreter ready. Type 'help' for available commands.");
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) continue;
            String[] tokens = line.split("\\s+");
            String command = tokens[0].toLowerCase();
            ConsoleExpression expression = expressions.get(command);
            if (expression != null) {
                try {
                    expression.interpret(new ConsoleContext(server, tokens));
                } catch (Exception e) {
                    log.error("[Console] Error executing '" + command + "': " + e.getMessage(), e);
                }
            } else {
                log.info("[Console] Unknown command: '" + command + "'. Type 'help' for available commands.");
            }
        }
    }
}
