package com.teachingtool.websocket.config;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

public class ChallengeListenerAppender extends AppenderBase<ILoggingEvent> {
    @Override
    protected void append(ILoggingEvent eventObject) {
        String message = eventObject.getFormattedMessage();
        // 监听因 token 为空触发的挑战
        if (message.contains("Sending WebSocket notification due to empty token.")) {
            handleEmptyTokenChallenge();
        }
        // 监听因用户 ID 不匹配触发的挑战
        if (message.contains("Challenge triggered because user ID mismatch.")) {
            handleInvalidTokenChallenge();
        }
        // 监听因未经授权的人修改会员属性触发的挑战
        if (message.contains("Member attributes have been modified by unauthorized persons")) {
            handleUnauthorizedModificationChallenge();
        }
    }

    private void handleInvalidTokenChallenge() {
        System.out.println("Challenge succeeded: Triggered by invalid token.");
        ChallengeWebSocketHandler.notifyClients("Challenge succeeded: Triggered by invalid token.");
    }

    private void handleEmptyTokenChallenge() {
        System.out.println("Challenge succeeded: Triggered by empty token.");
        ChallengeWebSocketHandler.notifyClients("Challenge succeeded: Triggered by empty token.");
    }

    private void handleUnauthorizedModificationChallenge() {
        System.out.println("Challenge succeeded: Triggered by unauthorized modification.");
        ChallengeWebSocketHandler.notifyClients("Challenge succeeded: Triggered by unauthorized modification.");
    }
}
