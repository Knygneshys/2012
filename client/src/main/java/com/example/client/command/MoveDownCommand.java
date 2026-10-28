package com.example.client.command;

import com.example.client.network.NetworkClient;

public class MoveDownCommand extends NetworkCommand {
    public MoveDownCommand(NetworkClient networkClient) {
        super(networkClient, "DOWN");
    }
}

