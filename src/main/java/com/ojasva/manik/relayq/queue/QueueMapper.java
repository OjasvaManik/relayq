package com.ojasva.manik.relayq.queue;

import com.ojasva.manik.relayq.queue.dto.GetQueueResponse;
import org.springframework.stereotype.Component;

@Component
public class QueueMapper {

    public GetQueueResponse toGetQueueResponse(Queue queue) {
        return new GetQueueResponse(
                queue.getId(),
                queue.getName(),
                queue.getWebhookUrl(),
                queue.getMaxRetries(),
                queue.getTenant().getName()
        );
    }

}
