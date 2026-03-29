package com.ojasva.manik.relayq.joblog;

import com.ojasva.manik.relayq.common.BaseTimeEntity;
import com.ojasva.manik.relayq.job.Job;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "job_logs")
@Getter
@Setter
@NoArgsConstructor
public class JobLog extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;
}