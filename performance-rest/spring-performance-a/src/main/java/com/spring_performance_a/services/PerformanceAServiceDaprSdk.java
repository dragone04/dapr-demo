package com.spring_performance_a.services;

import com.spring_performance_a.model.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import io.dapr.client.DaprClient;
import io.dapr.client.DaprClientBuilder;
import io.dapr.client.domain.HttpExtension;

import java.util.Map;

@RestController
public class PerformanceAServiceDaprSdk {

    private static final Logger LOGGER = LoggerFactory.getLogger(PerformanceAServiceDaprSdk.class);

    private static final String SERVICE_APP_ID = "spring-performance-b";
    private static final String SERVICE_METHOD = "callback";

    @PostMapping(path = "/callbackDaprSdk", consumes = MediaType.ALL_VALUE)
    public ResponseEntity<Message> callbackDaprSdk(
            @RequestHeader("traceparent") String traceparent,
            @RequestHeader("tracestate") String tracestate,
            @RequestBody(required = false) Message message
    ) {

        LOGGER.info("Received traceparent: {}", traceparent);
        LOGGER.info("Received tracestate: {}", tracestate);
        LOGGER.info("Received message: {}", message);

        Message response = null;
        Map<String, String> headers = Map.of("traceparent", traceparent, "tracestate", tracestate);

        try (DaprClient client = (new DaprClientBuilder()).build()) {

            response = client.invokeMethod(SERVICE_APP_ID, SERVICE_METHOD, message, HttpExtension.POST, headers, Message.class).block();
            LOGGER.info("Response: {}", response);

        } catch (Exception e) {
            LOGGER.error("Error serializing message", e);
        }

        return ResponseEntity.ok(response);

    }

}
