package com.spring.service.consumer.services.rest;

import com.spring.service.consumer.model.Message;
import io.dapr.client.DaprClient;
import io.dapr.client.DaprClientBuilder;
import io.dapr.client.domain.HttpExtension;
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
public class RestService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestService.class);

    private static final String SERVICE_APP_ID = "spring-service-mongo";
    private static final String SERVICE_METHOD = "save";

    @PostMapping(path = "/callback", consumes = MediaType.ALL_VALUE)
    public ResponseEntity<Message> callback(
            @RequestHeader(value = "traceparent", required = false) String traceparent,
            @RequestHeader(value = "tracestate", required = false) String tracestate,
            @RequestBody(required = false) Message message
    ) {

        LOGGER.info("Received message: {}", message);
        LOGGER.info("traceparent: {}", traceparent);
        LOGGER.info("tracestate: {}", tracestate);

        Message response;
        Map<String, String> headers = Map.of(
                "traceparent", Objects.toString(traceparent, ""),
                "tracestate", Objects.toString(tracestate, "")
        );

        try (DaprClient client = (new DaprClientBuilder()).build()) {

            response = client.invokeMethod(SERVICE_APP_ID, SERVICE_METHOD, message, HttpExtension.POST, headers, Message.class).block();
            LOGGER.info("Response: {}", response);

        } catch (Exception e) {
            LOGGER.error("Error serializing message", e);
        }

        return ResponseEntity.ok(message);

    }

}
