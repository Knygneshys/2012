package com.example.server.controller;

import com.example.server.GameServer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ServerStatusController {

    private final GameServer gameServer;

    public ServerStatusController(GameServer gameServer) {
        this.gameServer = gameServer;
    }

    @GetMapping("/status")
    public Map<String, Object> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("game", "Bomberman Server");
        status.put("running", gameServer.isRunning());
        status.put("port", gameServer.getPort());
        status.put("wsEndpoint", "/ws/game");
        status.put("connectedPlayers", gameServer.getConnectedPlayerCount());
        status.put("maxPlayers", gameServer.getMaxPlayers());
        return status;
    }

    @GetMapping("/players")
    public List<Map<String, Object>> getPlayers() {
        return gameServer.getPlayerSummaryList();
    }
}
