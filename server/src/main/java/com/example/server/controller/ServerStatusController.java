package com.example.server.controller;

import com.example.server.GameFacade;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ServerStatusController {

    private final GameFacade gameFacade;

    public ServerStatusController(GameFacade gameFacade) {
        this.gameFacade = gameFacade;
    }

    @GetMapping("/status")
    public Map<String, Object> getStatus() {
        return gameFacade.getStatus();
    }

    @GetMapping("/players")
    public List<Map<String, Object>> getPlayers() {
        return gameFacade.getPlayers();
    }
}
