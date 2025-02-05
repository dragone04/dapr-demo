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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Objects;

@RestController
public class ProducerDaprSdk {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProducerDaprSdk.class);

    private static final String PUBSUB_NAME = "messagepubsub";
    private static final String TOPIC_NAME = "my-topic";

    @PostMapping(path = "/produceDaprSdk", consumes = MediaType.ALL_VALUE)
    public ResponseEntity<Message> produceDaprSdk(
            @RequestHeader(value = "traceparent", required = false) String traceparent,
            @RequestHeader(value = "tracestate", required = false) String tracestate,
            @RequestBody(required = false) Message message
    ) {

        Map<String, String> headers = Map.of(
                "traceparent", Objects.toString(traceparent, ""),
                "tracestate", Objects.toString(tracestate, "")
        );

        try (DaprClient client = new DaprClientBuilder().build()) {
            client.publishEvent(
                    PUBSUB_NAME,
                    TOPIC_NAME,
                    message,
                    headers
            ).block();

        } catch (Exception e) {
            
            e.printStackTrace();
            throw new RuntimeException(e);

        } finally {
            LOGGER.info("Published data: {}", message);
        }

        return ResponseEntity.ok(message);

    }

}
