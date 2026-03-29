package com.ojasva.manik.relayq.auth;

import com.ojasva.manik.relayq.auth.dto.*;
import com.ojasva.manik.relayq.common.exception.BadRequestException;
import com.ojasva.manik.relayq.common.exception.ConflictException;
import com.ojasva.manik.relayq.common.exception.ResourceNotFoundException;
import com.ojasva.manik.relayq.config.JwtUtils;
import com.ojasva.manik.relayq.config.RedisService;
import com.ojasva.manik.relayq.tenant.Tenant;
import com.ojasva.manik.relayq.tenant.TenantRepository;
import com.ojasva.manik.relayq.user.User;
import com.ojasva.manik.relayq.user.UserPrincipal;
import com.ojasva.manik.relayq.user.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.UUID;

@Service
public class AuthService {

    private static final long OTP_TTL = 600L;           // 10 minutes
    private static final long RESET_TOKEN_TTL = 300L;   // 5 minutes
    private static final long SESSION_TTL = 86400L;     // 24 hours

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final RedisService redisService;

    public AuthService(TenantRepository tenantRepository,
                       UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtUtils jwtUtils,
                       RedisService redisService) {
        this.tenantRepository = tenantRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.redisService = redisService;
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("An account with this email already exists");
        }

        Tenant tenant = new Tenant();
        tenant.setName(request.orgName());
        tenantRepository.save(tenant);

        String tempPassword = generateTempPassword();

        User user = new User();
        user.setTenant(tenant);
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(tempPassword));
        user.setRole(User.Role.ADMIN);
        user.setTemporaryPassword(true);
        userRepository.save(user);

        // TODO: send tempPassword to email via JavaMailSender
        System.out.println("Temp password for " + request.email() + ": " + tempPassword);

        return new RegisterResponse("Account created. Check your email for your temporary password.");
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        assert principal != null;
        String jwt = jwtUtils.generateTokenFromUsername(principal);

        redisService.set("session:" + jwt, principal.getId().toString(), SESSION_TTL);

        return new AuthResponse(jwt);
    }

    public void logout(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new BadRequestException("Invalid authorization header");
        }
        String jwt = authHeader.substring(7);
        if (!redisService.hasKey("session:" + jwt)) {
            throw new BadRequestException("Session not found or already expired");
        }
        redisService.delete("session:" + jwt);
    }

    public void forgotPassword(ForgotPasswordRequest request) {
        if (!userRepository.existsByEmail(request.email())) {
            return;
        }
        String otp = generateOtp();
        redisService.set("otp:" + request.email(), otp, OTP_TTL);

        // TODO: send OTP to email via JavaMailSender
        System.out.println("OTP for " + request.email() + ": " + otp);
    }

    public VerifyOtpResponse verifyOtp(VerifyOtpRequest request) {
        String storedOtp = redisService.get("otp:" + request.email());

        if (storedOtp == null) {
            throw new BadRequestException("OTP expired or not found");
        }
        if (!storedOtp.equals(request.otp())) {
            throw new BadRequestException("Invalid OTP");
        }

        redisService.delete("otp:" + request.email());

        String resetToken = UUID.randomUUID().toString();
        redisService.set("reset_token:" + resetToken, request.email(), RESET_TOKEN_TTL);

        return new VerifyOtpResponse(resetToken, String.valueOf(RESET_TOKEN_TTL));
    }

    @Transactional
    public ResetPasswordResponse resetPassword(ResetPasswordRequest request, String authHeader) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assert authentication != null;
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

        assert principal != null;
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.oldPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        user.setTemporaryPassword(false);
        userRepository.save(user);

        String jwt = authHeader.substring(7);
        redisService.delete("session:" + jwt);
        return new ResetPasswordResponse("Password Reset Successful. Log in Again.", jwt);
    }

    private String generateOtp() {
        return String.format("%06d", new SecureRandom().nextInt(999999));
    }

    private String generateTempPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(12);
        for (int i = 0; i < 12; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}