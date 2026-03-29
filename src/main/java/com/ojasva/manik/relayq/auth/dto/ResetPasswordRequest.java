package com.ojasva.manik.relayq.auth.dto;

public record ResetPasswordRequest(
        String oldPassword,
        String newPassword
) {
}
