package com.teachingtool.websocket.config;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

public class ChallengeListenerAppender extends AppenderBase<ILoggingEvent> {
    @Override
    protected void append(ILoggingEvent eventObject) {
        String exceptionMessage = (eventObject.getThrowableProxy() != null) ? eventObject.getThrowableProxy().getMessage() : "";
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
        // 监听因无效的 membership 类型触发的挑战
        if (message.contains("Sending WebSocket notification due to invalid membership type.")) {
            handleInvalidMembershipTypeChallenge();
        }
        // 监听因购物车触发的挑战
        if (message.contains("Challenge triggered because user ID mismatch in cart.")) {
            handleCartChallenge();
        }
        // 监听因购物车触发的挑战
        if (message.contains("Sending WebSocket notification due to modify number.")) {
            handleModifyChallenge();
        }
    }

    private void handleInvalidTokenChallenge() {
        ChallengeWebSocketHandler.notifyClients("Challenge succeeded: Triggered by invalid token.");
    }

    private void handleEmptyTokenChallenge() {
        ChallengeWebSocketHandler.notifyClients("Challenge succeeded: Triggered by empty token.");
    }

    private void handleUnauthorizedModificationChallenge() {
        ChallengeWebSocketHandler.notifyClients("Challenge succeeded: Triggered by unauthorized modification.");
    }

    private void handleInvalidMembershipTypeChallenge() {
        ChallengeWebSocketHandler.notifyClients("Challenge succeeded: Triggered by invalid membership type.");
    }

    private void handleCartChallenge() {
        ChallengeWebSocketHandler.notifyClients("Challenge succeeded: Triggered by user ID mismatch.");
    }

    private void handleModifyChallenge() {
        ChallengeWebSocketHandler.notifyClients("Challenge succeeded: Triggered by modify number.");
    }
}
