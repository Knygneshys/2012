package com.example.client.command;

import com.example.client.network.NetworkClient;

import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

public class CommandDispatcher {
    private final Map<Integer, Command> commands = new HashMap<>();

    public CommandDispatcher(NetworkClient networkClient) {
        commands.put(KeyEvent.VK_W, new MoveUpCommand(networkClient));
        commands.put(KeyEvent.VK_S, new MoveDownCommand(networkClient));
        commands.put(KeyEvent.VK_A, new MoveLeftCommand(networkClient));
        commands.put(KeyEvent.VK_D, new MoveRightCommand(networkClient));
        // Arrow keys work too
        commands.put(KeyEvent.VK_UP, commands.get(KeyEvent.VK_W));
        commands.put(KeyEvent.VK_DOWN, commands.get(KeyEvent.VK_S));
        commands.put(KeyEvent.VK_LEFT, commands.get(KeyEvent.VK_A));
        commands.put(KeyEvent.VK_RIGHT, commands.get(KeyEvent.VK_D));
        commands.put(KeyEvent.VK_SPACE, new PlaceBombCommand(networkClient));
        commands.put(KeyEvent.VK_R, new ResetGameCommand(networkClient));
        commands.put(KeyEvent.VK_ENTER, new StartGameCommand(networkClient));
    }

    /** Binds an extra key, e.g. client-only commands such as opening the pause menu. */
    public void register(int keyCode, Command command) {
        commands.put(keyCode, command);
    }

    public void execute(int keyCode) {
        Command command = commands.get(keyCode);
        if (command != null) {
            command.execute();
        }
    }
}
