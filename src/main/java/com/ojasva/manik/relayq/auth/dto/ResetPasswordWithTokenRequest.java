package com.ojasva.manik.relayq.auth.dto;

public record ResetPasswordWithTokenRequest(
        String resetToken,
        String newPassword
) {
}
