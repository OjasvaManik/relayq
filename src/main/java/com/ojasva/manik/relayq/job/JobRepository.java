package com.ojasva.manik.relayq.job;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JobRepository extends JpaRepository<Job, UUID> {

    List<Job> findAllByTenantId(UUID tenantId);

    List<Job> findAllByTenantIdAndStatus(UUID tenantId, Job.Status status);

    List<Job> findAllByTenantIdAndCreatedById(UUID tenantId, UUID createdById);

    List<Job> findAllByQueueId(UUID queueId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT j FROM Job j
            WHERE j.status = 'PENDING'
            AND (j.scheduledAt IS NULL OR j.scheduledAt <= CURRENT_TIMESTAMP)
            ORDER BY j.priority DESC, j.createdAt ASC
            LIMIT 1
            """)
    Optional<Job> findNextPendingJob();

}