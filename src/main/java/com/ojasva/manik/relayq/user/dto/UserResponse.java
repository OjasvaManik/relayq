package com.ojasva.manik.relayq.user.dto;

import com.ojasva.manik.relayq.user.User;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String name,
        String email,
        String tenantName,
        User.Role role,
        boolean isTemporaryPassword,
        LocalDateTime createdAt
) {
}
