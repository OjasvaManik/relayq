package com.ojasva.manik.relayq.job;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JobRepository extends JpaRepository<Job, UUID> {

    Page<Job> findAllByTenantId(UUID tenantId, Pageable pageable);

    Page<Job> findAllByTenantIdAndCreatedById(UUID tenantId, UUID createdById, Pageable pageable);

    Optional<Job> findByIdAndCreatedBy_Id(UUID id, UUID userId);

    List<Job> findAllByTenantIdAndCreatedById(UUID tenantId, UUID createdById);

    List<Job> findAllByQueueId(UUID queueId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT j FROM Job j
            JOIN FETCH j.queue
            JOIN FETCH j.tenant
            JOIN FETCH j.createdBy
            WHERE j.status = 'PENDING'
            AND (j.scheduledAt IS NULL OR j.scheduledAt <= CURRENT_TIMESTAMP)
            ORDER BY j.priority DESC, j.createdAt ASC
            LIMIT 1
            """)
    Optional<Job> findNextPendingJob();

}