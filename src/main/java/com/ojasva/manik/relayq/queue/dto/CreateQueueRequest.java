package com.ojasva.manik.relayq.queue.dto;

import java.util.Optional;

public record CreateQueueRequest(
        String name,
        String webhookUrl,
        Optional<Integer> maxRetries
) {
}