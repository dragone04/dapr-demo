package com.spring.service.consumer.services.pubsub;

import com.spring.service.consumer.model.Message;
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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Objects;

@RestController
public class ConsumerDaprSdk {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerDaprSdk.class);

    private static final String PUBSUB_NAME = "messagepubsub";
    private static final String TOPIC_NAME = "messages";

    private static final String SERVICE_APP_ID = "spring-service-mongo";
    private static final String SERVICE_METHOD = "save";

    @Topic(name = TOPIC_NAME, pubsubName = PUBSUB_NAME)
    @PostMapping(path = "/consumeDaprSdk", consumes = MediaType.ALL_VALUE)
    public Mono<Void> consumeDaprSdk(
            @RequestHeader(value = "traceparent", required = false) String traceparent,
            @RequestHeader(value = "tracestate", required = false) String tracestate,
            @RequestBody(required = false) CloudEvent<Message> cloudEvent
    ) {

        return Mono.fromRunnable(() -> {
            try {

                LOGGER.info("traceparent: {}", traceparent);
                LOGGER.info("tracestate: {}", tracestate);

                LOGGER.info("cloudEvent@traceparent: {}", cloudEvent.getTraceParent());
                LOGGER.info("cloudEvent@tracestate: {}", cloudEvent.getTraceState());

                Message response;
                Map<String, String> headers = Map.of(
                        "traceparent", Objects.toString(traceparent, ""),
                        "tracestate", Objects.toString(tracestate, "")
                );

                try (DaprClient client = (new DaprClientBuilder()).build()) {

                    response = client.invokeMethod(SERVICE_APP_ID, SERVICE_METHOD, cloudEvent.getData(), HttpExtension.POST, headers, Message.class).block();
                    LOGGER.info("response: {}", response);

                } catch (Exception e) {
                    e.printStackTrace();
                }

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

    }

}
