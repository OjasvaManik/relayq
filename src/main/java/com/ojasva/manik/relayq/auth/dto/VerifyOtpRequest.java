package com.ojasva.manik.relayq.auth.dto;

public record VerifyOtpRequest(
        String email,
        String otp
) {
}
