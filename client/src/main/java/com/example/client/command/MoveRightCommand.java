package com.example.client.command;

import com.example.client.network.NetworkClient;

public class MoveRightCommand extends NetworkCommand {
    public MoveRightCommand(NetworkClient networkClient) {
        super(networkClient, "RIGHT");
    }
}

