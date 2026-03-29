package com.ojasva.manik.relayq.auth.dto;

public record VerifyOtpResponse(
        String resetToken,
        String expiresIn
) {
}
