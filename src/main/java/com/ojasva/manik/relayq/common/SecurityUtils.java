package com.ojasva.manik.relayq.common;

import com.ojasva.manik.relayq.user.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    private SecurityUtils() {
    }

    public static UserPrincipal getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            throw new IllegalStateException("No authenticated user found");
        }
        return principal;
    }
}