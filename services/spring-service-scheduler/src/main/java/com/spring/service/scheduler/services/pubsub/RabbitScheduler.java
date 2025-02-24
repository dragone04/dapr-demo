package com.spring.service.scheduler.services.pubsub;

import com.spring.service.scheduler.model.ExternalServiceObject;
import com.spring.service.scheduler.model.Message;
import io.dapr.client.DaprClient;
import io.dapr.client.DaprClientBuilder;
import io.dapr.client.domain.HttpExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;

@RestController
public class RabbitScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(RabbitScheduler.class);

    private static final String SERVICE_APP_ID = "spring-service-producer";
    private static final String SERVICE_METHOD = "produceDaprSdk";

    private static final String EXTERNAL_HTTP_ENDPOINT = "external-http-endpoint";
    private static final String EXTERNAL_HTTP_METHOD = "todos";

    @PostMapping(path = "/rabbitScheduler")
    public ResponseEntity<Message> rabbitScheduler(
            @RequestHeader(value = "traceparent", required = false) String traceparent,
            @RequestHeader(value = "tracestate", required = false) String tracestate
    ) {

        LOGGER.info("traceparent: {}", traceparent);
        LOGGER.info("tracestate: {}", tracestate);

        ExternalServiceObject response;
        Message springProducerResponse = new Message();

        Random random = new Random();
        int randomNumber = random.nextInt(100) + 1;

        Map<String, String> headers = Map.of(
                "traceparent", Objects.toString(traceparent, ""),
                "tracestate", Objects.toString(tracestate, "")
        );

        try (DaprClient client = (new DaprClientBuilder()).build()) {

            response = client.invokeMethod(EXTERNAL_HTTP_ENDPOINT, EXTERNAL_HTTP_METHOD + "/" + randomNumber, null, HttpExtension.GET, headers, ExternalServiceObject.class).block();
            LOGGER.info("response: {}", response);

            if (response != null) {
                Message msg = new Message(response.getId(), response.getTitle());
                springProducerResponse = client.invokeMethod(SERVICE_APP_ID, SERVICE_METHOD, msg, HttpExtension.POST, headers, Message.class).block();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return ResponseEntity.ok(springProducerResponse);

    }

}
