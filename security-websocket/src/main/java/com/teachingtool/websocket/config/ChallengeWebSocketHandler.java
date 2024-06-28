package com.teachingtool.websocket.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
@Slf4j
public class ChallengeWebSocketHandler extends TextWebSocketHandler {

    private static final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        log.info("WebSocket connection established with session id: {}", session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
        log.info("WebSocket connection closed with session id: {}, reason: {}, code: {}", session.getId(), status.getReason(), status.getCode());
    }

    public static void notifyClients(String message) {
        for (WebSocketSession session : sessions) {
            try {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(message));
                } else {
                    log.warn("WebSocket session {} is closed, removing from sessions", session.getId());
                    sessions.remove(session); // 移除已关闭的会话
                }
            } catch (IOException e) {
                log.error("Failed to send message to WebSocket session {}: {}", session.getId(), e.getMessage());
                sessions.remove(session); // 如果发送消息失败，也移除该会话
            }
        }
    }
}
