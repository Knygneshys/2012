package com.example.client.command;

import com.example.client.network.NetworkClient;

public class MoveUpCommand extends NetworkCommand {
    public MoveUpCommand(NetworkClient networkClient) {
        super(networkClient, "UP");
    }
}

