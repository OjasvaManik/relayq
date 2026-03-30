package com.ojasva.manik.relayq.user.dto;

import java.util.Optional;

public record UpdateQueueRequest(
        Optional<String> webhookUrl,
        Optional<Integer> maxRetries
) {
}
