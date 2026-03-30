package com.ojasva.manik.relayq.job;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
public class WebhookService {

    private final RestClient restClient;

    public WebhookService(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.build();
    }

    public void deliver(Job job) {
        restClient.post()
                .uri(job.getQueue().getWebhookUrl())
                .body(Map.of(
                        "jobId", job.getId().toString(),
                        "payload", job.getPayload()
                ))
                .retrieve()
                .toBodilessEntity();
    }
}