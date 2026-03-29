package com.ojasva.manik.relayq.auth.dto;

public record RegisterRequest(
        String orgName,
        String name,
        String email
) {
}
