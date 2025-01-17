package com.spring.service.producer.services.rest;

import com.spring.service.producer.model.Message;
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
import java.util.Objects;

@RestController
public class RestServiceDaprSdk {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestServiceDaprSdk.class);

    private static final String SERVICE_APP_ID = "spring-service-consumer";
    private static final String SERVICE_METHOD = "callback";

    @PostMapping(path = "/callbackDaprSdk", consumes = MediaType.ALL_VALUE)
    public ResponseEntity<Message> callbackDaprSdk(
            @RequestHeader(value = "traceparent", required = false) String traceparent,
            @RequestHeader(value = "tracestate", required = false) String tracestate,
            @RequestBody(required = false) Message message
    ) {

        LOGGER.info("Received message: {}", message);
        LOGGER.info("traceparent: {}", traceparent);
        LOGGER.info("tracestate: {}", tracestate);

        Message response = null;
        Map<String, String> headers = Map.of(
                "traceparent", Objects.toString(traceparent, ""),
                "tracestate", Objects.toString(tracestate, "")
        );

        try (DaprClient client = (new DaprClientBuilder()).build()) {

            response = client.invokeMethod(SERVICE_APP_ID, SERVICE_METHOD, message, HttpExtension.POST, headers, Message.class).block();
            LOGGER.info("Response: {}", response);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return ResponseEntity.ok(response);

    }

}