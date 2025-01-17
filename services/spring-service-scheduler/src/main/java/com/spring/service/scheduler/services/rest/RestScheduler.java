package com.spring.service.scheduler.services.rest;

import com.spring.service.scheduler.model.ExternalServiceObject;
import com.spring.service.scheduler.model.Message;
import io.dapr.client.domain.HttpExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import io.dapr.client.DaprClient;
import io.dapr.client.DaprClientBuilder;

import java.util.Map;
import java.util.Objects;
import java.util.Random;

@RestController
public class RestScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestScheduler.class);

    private static final String SERVICE_APP_ID = "spring-service-producer";
    private static final String SERVICE_METHOD = "callbackDaprSdk";

    private static final String EXTERNAL_HTTP_ENDPOINT = "external-http-endpoint";
    private static final String EXTERNAL_HTTP_METHOD = "todos";

    @PostMapping(path = "/restScheduler")
    public ResponseEntity<Message> restScheduler(
            @RequestHeader(value = "traceparent", required = false) String traceparent,
            @RequestHeader(value = "tracestate", required = false) String tracestate
    ) {

        ExternalServiceObject response;
        Message springProducerResponse = new Message();

        Random random = new Random();
        int randomNumber = random.nextInt(100) + 1;

        Map<String, String> headers = Map.of(
                "traceparent", Objects.toString(traceparent, ""),
                "tracestate", Objects.toString(tracestate, "")
        );

        try (DaprClient client = (new DaprClientBuilder()).build()) {

            response = client.invokeMethod(EXTERNAL_HTTP_ENDPOINT, EXTERNAL_HTTP_METHOD + "/" + randomNumber, HttpExtension.GET, headers, ExternalServiceObject.class).block();

            if (response != null) {
                Message msg = new Message(response.getId(), response.getTitle());
                springProducerResponse = client.invokeMethod(SERVICE_APP_ID, SERVICE_METHOD, msg, HttpExtension.POST, headers, Message.class).block();
            }
        } catch (Exception e) {
            LOGGER.error("Error serializing message", e);
        }

        LOGGER.info("response: {}", springProducerResponse);
        return ResponseEntity.ok(springProducerResponse);

    }

}
