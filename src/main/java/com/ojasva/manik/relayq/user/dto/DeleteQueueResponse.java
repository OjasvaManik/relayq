package com.ojasva.manik.relayq.user.dto;

public record DeleteQueueResponse(
        String message,
        boolean deleted
) {
}
