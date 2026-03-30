package com.ojasva.manik.relayq.job.dto;

public record RescheduleJobResponse(
        String message,
        boolean rescheduled
) {
}
