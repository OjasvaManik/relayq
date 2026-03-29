package com.ojasva.manik.relayq.auth.dto;

public record LoginRequest(
        String email,
        String password
) {
}
