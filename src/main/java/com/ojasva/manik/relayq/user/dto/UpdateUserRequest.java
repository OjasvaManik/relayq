package com.ojasva.manik.relayq.user.dto;

import com.ojasva.manik.relayq.user.User;

import java.util.Optional;

public record UpdateUserRequest(
        Optional<String> name,
        Optional<User.Role> role
) {
}