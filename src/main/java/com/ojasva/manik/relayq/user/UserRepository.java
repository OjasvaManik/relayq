package com.ojasva.manik.relayq.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findAllByTenantId(UUID tenantId);

    Optional<User> findByIdAndTenantId(UUID id, UUID tenantId);
}