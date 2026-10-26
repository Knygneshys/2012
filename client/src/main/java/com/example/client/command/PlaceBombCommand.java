package com.example.client.command;

import com.example.client.network.NetworkClient;

public class PlaceBombCommand extends NetworkCommand {
    public PlaceBombCommand(NetworkClient networkClient) {
        super(networkClient, "BOMB");
    }
}

