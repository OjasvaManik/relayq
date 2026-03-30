package com.ojasva.manik.relayq.queue;

import com.ojasva.manik.relayq.common.SecurityUtils;
import com.ojasva.manik.relayq.common.exception.ResourceNotFoundException;
import com.ojasva.manik.relayq.queue.dto.CreateQueueRequest;
import com.ojasva.manik.relayq.queue.dto.CreateQueueResponse;
import com.ojasva.manik.relayq.queue.dto.GetQueueResponse;
import com.ojasva.manik.relayq.tenant.Tenant;
import com.ojasva.manik.relayq.tenant.TenantRepository;
import com.ojasva.manik.relayq.user.UserPrincipal;
import com.ojasva.manik.relayq.user.dto.DeleteQueueResponse;
import com.ojasva.manik.relayq.user.dto.UpdateQueueRequest;
import com.ojasva.manik.relayq.user.dto.UpdateQueueResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class QueueService {

    private final QueueRepository queueRepository;
    private final TenantRepository tenantRepository;
    private final QueueMapper queueMapper;

    public QueueService(QueueRepository queueRepository,
                        TenantRepository tenantRepository,
                        QueueMapper queueMapper) {
        this.queueRepository = queueRepository;
        this.tenantRepository = tenantRepository;
        this.queueMapper = queueMapper;
    }

    public CreateQueueResponse createQueue(CreateQueueRequest createQueueRequest) {
        UserPrincipal principal = SecurityUtils.getCurrentUser();
        assert principal != null;

        Tenant tenant = tenantRepository.findById(principal.getTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found"));

        Queue queue = new Queue();
        queue.setName(createQueueRequest.name());
        queue.setWebhookUrl(createQueueRequest.webhookUrl());
        queue.setMaxRetries(createQueueRequest.maxRetries().orElse(3));
        queue.setTenant(tenant);

        queueRepository.save(queue);

        return new CreateQueueResponse("Queue created successfully");
    }

    public List<GetQueueResponse> getQueues() {
        UserPrincipal principal = SecurityUtils.getCurrentUser();

        assert principal != null;
        return queueRepository.findAllByTenantId(principal.getTenantId())
                .stream()
                .map(queueMapper::toGetQueueResponse)
                .toList();
    }

    public GetQueueResponse getQueue(UUID queueId) {
        UserPrincipal principal = SecurityUtils.getCurrentUser();
        assert principal != null;

        return queueMapper.toGetQueueResponse(queueRepository.findByIdAndTenantId(queueId, principal.getTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Queue not found")));
    }

    public UpdateQueueResponse updateQueue(UUID id, UpdateQueueRequest request) {
        UserPrincipal principal = SecurityUtils.getCurrentUser();
        assert principal != null;

        Queue queue = queueRepository.findByIdAndTenantId(id, principal.getTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Queue not found"));
        request.webhookUrl().ifPresent(queue::setWebhookUrl);
        request.maxRetries().ifPresent(queue::setMaxRetries);
        queueRepository.save(queue);

        return new UpdateQueueResponse("Queue Updated Successfully", true);
    }

    public DeleteQueueResponse deleteQueue(UUID id) {
        UserPrincipal principal = SecurityUtils.getCurrentUser();
        assert principal != null;
        Queue queue = queueRepository.findByIdAndTenantId(id, principal.getTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Queue not found"));
        queueRepository.delete(queue);
        return new DeleteQueueResponse("Queue Deleted Successfully", true);
    }

}
