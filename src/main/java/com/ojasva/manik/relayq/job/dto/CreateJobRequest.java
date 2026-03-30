package com.ojasva.manik.relayq.job.dto;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public record CreateJobRequest(
        UUID queueId,
        Object payload,
        Optional<Integer> priority,
        Optional<LocalDateTime> scheduledAt
) {
}