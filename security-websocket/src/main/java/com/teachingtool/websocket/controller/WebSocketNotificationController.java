package com.teachingtool.websocket.controller;

import com.teachingtool.websocket.config.ChallengeWebSocketHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WebSocketNotificationController {

    @PostMapping("/notify")
    public void notifyClients(@RequestBody String message) {
        ChallengeWebSocketHandler.notifyClients(message);
    }
}
