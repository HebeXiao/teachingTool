package com.teachingtool.websocket.config;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

public class ChallengeListenerAppender extends AppenderBase<ILoggingEvent> {
    @Override
    protected void append(ILoggingEvent eventObject) {
        String message = eventObject.getFormattedMessage();
        if (message.contains("Challenge triggered because no valid token was provided.")) {
            handleChallengeTriggered();
        }
    }

    private void handleChallengeTriggered() {
        System.out.println("Challenge succeeded: Triggered by invalid token.");
        ChallengeWebSocketHandler.notifyClients("Challenge succeeded: Triggered by invalid token.");
    }
}
