package com.spring_performance_b.services.pubsub;

import com.spring_performance_b.model.Message;
import io.dapr.Topic;
import io.dapr.client.DaprClient;
import io.dapr.client.DaprClientBuilder;
import io.dapr.client.domain.CloudEvent;
import io.dapr.client.domain.HttpExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
public class ConsumerDaprSdk {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerDaprSdk.class);

    private static final String PUBSUB_NAME = "messagepubsub";
    private static final String TOPIC_NAME = "messages";

    private static final String SERVICE_APP_ID = "spring-performance-c";
    private static final String SERVICE_METHOD = "save";

    @Topic(name = TOPIC_NAME, pubsubName = PUBSUB_NAME)
    @PostMapping(path = "/consumeDaprSdk", consumes = MediaType.ALL_VALUE)
    public Mono<Void> consumeDaprSdk(
            @RequestBody(required = false) CloudEvent<Message> cloudEvent
    ) {

        return Mono.fromRunnable(() -> {
            try {

                LOGGER.info("Received traceparent: {}", cloudEvent.getTraceParent());
                LOGGER.info("Received tracestate: {}", cloudEvent.getTraceState());
                LOGGER.info("Received pubSubName: {}", cloudEvent.getPubsubName());
                LOGGER.info("Received id: {}", cloudEvent.getId());
                LOGGER.info("Received topic: {}", cloudEvent.getTopic());
                LOGGER.info("Received message getData: {}", cloudEvent.getData());

                Message response;
                Map<String, String> headers = Map.of("traceparent", cloudEvent.getTraceParent(), "tracestate", cloudEvent.getTraceState());

                try (DaprClient client = (new DaprClientBuilder()).build()) {

                    response = client.invokeMethod(SERVICE_APP_ID, SERVICE_METHOD, cloudEvent.getData(), HttpExtension.POST, headers, Message.class).block();
                    LOGGER.info("Response: {}", response);

                } catch (Exception e) {
                    LOGGER.error("Error serializing message", e);
                }

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });


    }

}
