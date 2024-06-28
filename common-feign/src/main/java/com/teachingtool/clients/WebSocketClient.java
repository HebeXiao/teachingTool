package com.teachingtool.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "websocket-service")
public interface WebSocketClient {

    @PostMapping("/notify")
    void notifyClients(@RequestBody String message);
}
