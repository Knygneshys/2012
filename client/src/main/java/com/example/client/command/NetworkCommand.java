package com.example.client.command;

import com.example.client.network.NetworkClient;

public class NetworkCommand implements Command {
    private final NetworkClient networkClient;
    private final String action;

    public NetworkCommand(NetworkClient networkClient, String action) {
        this.networkClient = networkClient;
        this.action = action;
    }

    @Override
    public void execute() {
        networkClient.sendPlayerInput(action);
    }
}

