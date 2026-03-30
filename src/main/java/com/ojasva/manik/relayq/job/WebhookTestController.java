package com.ojasva.manik.relayq.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class WebhookTestController {

    private static final Logger logger = LoggerFactory.getLogger(WebhookTestController.class);

    @PostMapping("/webhook")
    public ResponseEntity<?> fakeWebhook(@RequestBody Object payload) {
        logger.info("Webhook received: {}", payload);
        return ResponseEntity.ok().build();
    }
}
