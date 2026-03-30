package com.ojasva.manik.relayq.job.dto;

import java.time.LocalDateTime;
import java.util.Optional;

public record RescheduleJobRequest(
        Optional<LocalDateTime> scheduledAt
) {
}
