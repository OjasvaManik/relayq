package com.ojasva.manik.relayq.user;

import com.ojasva.manik.relayq.user.dto.UserResponse;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getTenant().getName(),
                user.getRole(),
                user.isTemporaryPassword(),
                user.getCreatedAt()
        );
    }
}