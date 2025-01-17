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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;

@RestController
public class RabbitScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(RabbitScheduler.class);

    private static final String SERVICE_APP_ID = "spring-service-producer";
    private static final String SERVICE_METHOD = "produceDaprSdk";

    private static final String EXTERNAL_HTTP_ENDPOINT = "external-http-endpoint";
    private static final String EXTERNAL_HTTP_METHOD = "todos";

    @PostMapping(path = "/rabbitScheduler")
    public ResponseEntity<Message> rabbitScheduler() {

        ExternalServiceObject response;
        Message springProducerResponse = new Message();

        Random random = new Random();
        int randomNumber = random.nextInt(100) + 1;

        try (DaprClient client = (new DaprClientBuilder()).build()) {

            response = client.invokeMethod(EXTERNAL_HTTP_ENDPOINT, EXTERNAL_HTTP_METHOD + "/" + randomNumber, null, HttpExtension.GET, null, ExternalServiceObject.class).block();
            LOGGER.info("Response: {}", response);

            if (response != null) {
                Message msg = new Message(response.getId(), response.getTitle());
                springProducerResponse = client.invokeMethod(SERVICE_APP_ID, SERVICE_METHOD, msg, HttpExtension.POST, null, Message.class).block();
            }
        } catch (Exception e) {
            LOGGER.error("Error serializing message", e);
        }

        return ResponseEntity.ok(springProducerResponse);

    }

}