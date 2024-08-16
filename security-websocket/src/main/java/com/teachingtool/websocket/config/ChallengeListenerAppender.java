package com.teachingtool.websocket.config;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

public class ChallengeListenerAppender extends AppenderBase<ILoggingEvent> {
    @Override
    protected void append(ILoggingEvent eventObject) {
        String message = eventObject.getFormattedMessage();
        // Listen for challenges triggered by a null token
        if (message.contains("Sending WebSocket notification due to empty token.")) {
            handleEmptyTokenChallenge();
        }
        // Listening for challenges triggered by user ID mismatches
        if (message.contains("Challenge triggered because user ID mismatch.")) {
            handleInvalidTokenChallenge();
        }
        // Listen to challenges triggered by unauthorized people modifying member attributes
        if (message.contains("Member attributes have been modified by unauthorized persons")) {
            handleUnauthorizedModificationChallenge();
        }
        // Listening for challenges triggered by invalid membership types
        if (message.contains("Sending WebSocket notification due to invalid membership type.")) {
            handleInvalidMembershipTypeChallenge();
        }
        // Listening to challenges triggered by shopping carts
        if (message.contains("Challenge triggered because user ID mismatch in cart.")) {
            handleCartChallenge();
        }
        // Listening to challenges due to modification of the number of products
        if (message.contains("Sending WebSocket notification due to modify number.")) {
            handleModifyChallenge();
        }
    }

    private void handleInvalidTokenChallenge() {
        ChallengeWebSocketHandler.notifyClients("Challenge succeeded: Triggered by invalid token.");
    }

    private void handleEmptyTokenChallenge() {
        ChallengeWebSocketHandler.notifyClients("Challenge failed: Triggered by empty token.");
    }

    private void handleUnauthorizedModificationChallenge() {
        ChallengeWebSocketHandler.notifyClients("Challenge succeeded: Triggered by unauthorized modification.");
    }

    private void handleInvalidMembershipTypeChallenge() {
        ChallengeWebSocketHandler.notifyClients("Challenge failed: Triggered by invalid membership type.");
    }

    private void handleCartChallenge() {
        ChallengeWebSocketHandler.notifyClients("Challenge succeeded: Triggered by user ID mismatch.");
    }

    private void handleModifyChallenge() {
        ChallengeWebSocketHandler.notifyClients("Challenge succeeded: Triggered by modify number.");
    }
}
