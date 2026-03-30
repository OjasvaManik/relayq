package com.ojasva.manik.relayq.job.dto;

import com.ojasva.manik.relayq.job.Job;

import java.time.LocalDateTime;
import java.util.UUID;

public record GetJobResponse(
        UUID id,
        Job.Status status,
        int priority,
        Object payload,
        int retryCount,
        int maxRetries,
        String errorMessage,
        LocalDateTime scheduledAt,
        LocalDateTime startedAt,
        LocalDateTime completedAt,
        LocalDateTime createdAt
) {
}
