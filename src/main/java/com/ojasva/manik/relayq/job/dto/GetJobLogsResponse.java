package com.ojasva.manik.relayq.job.dto;

import java.time.LocalDateTime;

public record GetJobLogsResponse(
        String message,
        LocalDateTime loggedAt
) {
}
