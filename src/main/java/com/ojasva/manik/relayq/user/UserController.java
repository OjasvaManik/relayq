package com.ojasva.manik.relayq.user;

import com.ojasva.manik.relayq.auth.AuthService;
import com.ojasva.manik.relayq.auth.dto.ResetPasswordRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final AuthService authService;

    public UserController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/me/password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request, @RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(authService.resetPassword(request, authHeader));
    }

}
