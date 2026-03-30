package com.ojasva.manik.relayq.user;

import com.ojasva.manik.relayq.auth.AuthService;
import com.ojasva.manik.relayq.auth.dto.ResetPasswordRequest;
import com.ojasva.manik.relayq.user.dto.UpdateUserRequest;
import com.ojasva.manik.relayq.user.dto.UserRegisterRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final AuthService authService;
    private final UserService userService;

    public UserController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> registerMember(@RequestBody UserRegisterRequest userRegisterRequest) {
        return ResponseEntity.ok(userService.register(userRegisterRequest));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUsers() {
        return ResponseEntity.ok(userService.getUsers());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUser(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getUser(id));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUser(@PathVariable UUID id, @RequestBody UpdateUserRequest user) {
        return ResponseEntity.ok(userService.updateUser(id, user));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        return ResponseEntity.ok(userService.getCurrentUser());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.deleteUser(id));
    }

    @PostMapping("/me/password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request, @RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(authService.resetPassword(request, authHeader));
    }

}
