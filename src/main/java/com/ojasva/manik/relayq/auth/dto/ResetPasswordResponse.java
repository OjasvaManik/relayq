package com.ojasva.manik.relayq.auth.dto;

public record ResetPasswordResponse(
        String message,
        String jwt
) {
}
