package com.spring.service.producer.services.pubsub;

import com.spring.service.producer.model.Message;
import io.dapr.client.DaprClient;
import io.dapr.client.DaprClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProducerDaprSdk {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProducerDaprSdk.class);

    private static final String PUBSUB_NAME = "messagepubsub";
    private static final String TOPIC_NAME = "messages";

    @PostMapping(path = "/produceDaprSdk", consumes = MediaType.ALL_VALUE)
    public ResponseEntity<Message> produceDaprSdk(
            @RequestBody(required = false) Message message
    ) {

        try (DaprClient client = new DaprClientBuilder().build()) {
            client.publishEvent(
                    PUBSUB_NAME,
                    TOPIC_NAME,
                    message
            ).block();

        } catch (Exception e) {
            LOGGER.error("Error: {}", e.getMessage());
            throw new RuntimeException(e);
        } finally {
            LOGGER.info("Published data: {}", message);
        }

        return ResponseEntity.ok(message);

    }

}
