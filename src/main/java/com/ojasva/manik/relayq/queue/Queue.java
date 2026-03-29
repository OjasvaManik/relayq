package com.ojasva.manik.relayq.queue;

import com.ojasva.manik.relayq.common.BaseTimeEntity;
import com.ojasva.manik.relayq.tenant.Tenant;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(
        name = "queues",
        uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id", "name"})
)
@Getter
@Setter
@NoArgsConstructor
public class Queue extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "webhook_url", nullable = false)
    private String webhookUrl;

    @Column(name = "max_retries", nullable = false)
    private int maxRetries = 3;
}