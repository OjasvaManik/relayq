package com.ojasva.manik.relayq.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ojasva.manik.relayq.user.UserPrincipal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

@Component
public class TemporaryPasswordFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;

    public TemporaryPasswordFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal principal) {
            if (principal.isTemporaryPassword()) {
                String path = request.getServletPath();

                if (!path.equals("/api/v1/users/me/password")) {
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);

                    Map<String, Object> body = Map.of(
                            "status", 403,
                            "error", "Forbidden",
                            "message", "You must change your temporary password before continuing",
                            "path", path
                    );

                    objectMapper.writeValue(response.getOutputStream(), body);
                    return;
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}