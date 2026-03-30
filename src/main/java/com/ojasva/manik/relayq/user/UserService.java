package com.ojasva.manik.relayq.user;

import com.ojasva.manik.relayq.auth.AuthService;
import com.ojasva.manik.relayq.auth.dto.RegisterResponse;
import com.ojasva.manik.relayq.common.MailService;
import com.ojasva.manik.relayq.common.SecurityUtils;
import com.ojasva.manik.relayq.common.exception.BadRequestException;
import com.ojasva.manik.relayq.common.exception.ConflictException;
import com.ojasva.manik.relayq.common.exception.ResourceNotFoundException;
import com.ojasva.manik.relayq.tenant.Tenant;
import com.ojasva.manik.relayq.tenant.TenantRepository;
import com.ojasva.manik.relayq.user.dto.DeleteUserResponse;
import com.ojasva.manik.relayq.user.dto.UpdateUserRequest;
import com.ojasva.manik.relayq.user.dto.UserRegisterRequest;
import com.ojasva.manik.relayq.user.dto.UserResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;
    private final TenantRepository tenantRepository;

    public UserService(UserRepository userRepository,
                       UserMapper userMapper,
                       PasswordEncoder passwordEncoder,
                       MailService mailService,
                       TenantRepository tenantRepository) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.mailService = mailService;
        this.tenantRepository = tenantRepository;
    }

    @Transactional
    public RegisterResponse register(UserRegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("An account with this email already exists");
        }
        UserPrincipal principal = SecurityUtils.getCurrentUser();
        Tenant tenant = tenantRepository.findById(principal.getTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found"));
        String tempPassword = AuthService.generateTempPassword();

        User user = new User();
        user.setTenant(tenant);
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(tempPassword));
        user.setRole(User.Role.MEMBER);
        user.setTemporaryPassword(true);
        userRepository.save(user);

        mailService.sendCreationMessage(request.email(), tempPassword, tenant.getName());

        return new RegisterResponse("Account created. Check your email for your temporary password.");
    }

    public List<UserResponse> getUsers() {
        UserPrincipal principal = SecurityUtils.getCurrentUser();

        assert principal != null;
        return userRepository.findAllByTenantId(principal.getTenantId())
                .stream()
                .map(userMapper::toResponse)
                .toList();
    }

    public UserResponse getUser(UUID id) {
        UserPrincipal principal = SecurityUtils.getCurrentUser();

        assert principal != null;
        User user = userRepository.findByIdAndTenantId(id, principal.getTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return userMapper.toResponse(user);
    }

    @Transactional
    public UserResponse updateUser(UUID targetUserId, UpdateUserRequest request) {
        UserPrincipal principal = SecurityUtils.getCurrentUser();
        User target = userRepository.findByIdAndTenantId(targetUserId, principal.getTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (request.role().isPresent()) {
            if (targetUserId.equals(principal.getId())) {
                throw new BadRequestException("You cannot change your own role");
            }
            target.setRole(request.role().get());
        }
        request.name().ifPresent(target::setName);

        return userMapper.toResponse(userRepository.save(target));
    }

    public UserResponse getCurrentUser() {
        UserPrincipal principal = SecurityUtils.getCurrentUser();
        assert principal != null;
        User user = userRepository.findByIdAndTenantId(principal.getId(), principal.getTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return userMapper.toResponse(user);
    }

    public DeleteUserResponse deleteUser(UUID id) {
        UserPrincipal principal = SecurityUtils.getCurrentUser();
        assert principal != null;

        User user = userRepository.findByIdAndTenantId(id, principal.getTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        userRepository.delete(user);
        return new DeleteUserResponse("User Deleted Successfully", true);
    }

}
