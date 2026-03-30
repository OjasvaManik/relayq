package com.ojasva.manik.relayq.queue;

import com.ojasva.manik.relayq.queue.dto.CreateQueueRequest;
import com.ojasva.manik.relayq.user.dto.UpdateQueueRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/queues")
public class QueueController {

    private final QueueService queueService;

    public QueueController(QueueService queueService) {
        this.queueService = queueService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createQueue(@RequestBody CreateQueueRequest createQueueRequest) {
        return ResponseEntity.ok(queueService.createQueue(createQueueRequest));
    }

    @GetMapping
    public ResponseEntity<?> getQueues() {
        return ResponseEntity.ok(queueService.getQueues());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getQueue(@PathVariable UUID id) {
        return ResponseEntity.ok(queueService.getQueue(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> updateQueue(@PathVariable UUID id, @RequestBody UpdateQueueRequest request) {
        return ResponseEntity.ok(queueService.updateQueue(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteQueue(@PathVariable UUID id) {
        return ResponseEntity.ok(queueService.deleteQueue(id));
    }

}
