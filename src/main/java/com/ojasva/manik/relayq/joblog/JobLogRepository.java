package com.ojasva.manik.relayq.joblog;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JobLogRepository extends JpaRepository<JobLog, UUID> {

    List<JobLog> findAllByJobIdOrderByCreatedAtAsc(UUID jobId);

}