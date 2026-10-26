package com.example.client.command;

import com.example.client.network.NetworkClient;

import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

public class CommandDispatcher {
    private final Map<Integer, Command> commands = new HashMap<>();

    public CommandDispatcher(NetworkClient networkClient) {
        commands.put(KeyEvent.VK_W, new NetworkCommand(networkClient, "UP"));
        commands.put(KeyEvent.VK_S, new NetworkCommand(networkClient, "DOWN"));
        commands.put(KeyEvent.VK_A, new NetworkCommand(networkClient, "LEFT"));
        commands.put(KeyEvent.VK_D, new NetworkCommand(networkClient, "RIGHT"));
        commands.put(KeyEvent.VK_SPACE, new NetworkCommand(networkClient, "BOMB"));
        commands.put(KeyEvent.VK_R, new NetworkCommand(networkClient, "RESET"));
    }

    public void execute(int keyCode) {
        Command command = commands.get(keyCode);
        if (command != null) {
            command.execute();
        }
    }
}

