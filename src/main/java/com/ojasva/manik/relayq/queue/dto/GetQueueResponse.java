package com.ojasva.manik.relayq.queue.dto;

import java.util.UUID;

public record GetQueueResponse(
        UUID id,
        String name,
        String webhookUrl,
        int maxRetries,
        String tenantName
) {
}
