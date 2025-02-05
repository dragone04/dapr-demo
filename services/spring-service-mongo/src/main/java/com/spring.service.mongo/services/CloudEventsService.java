package com.spring.service.mongo.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.service.mongo.model.CustomerManagementItem;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;
import io.cloudevents.core.data.PojoCloudEventData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.Objects;
import java.util.UUID;

@RestController
public class CloudEventsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CloudEventsService.class);

    ObjectMapper objectMapper;

    public CloudEventsService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostMapping("/cloudEvents")
    public ResponseEntity<CloudEvent> cloudEvents(
            @RequestBody CustomerManagementItem customerManagementItem,
            @RequestHeader HttpHeaders headers
    ) {

        CloudEvent cloudEvent = CloudEventBuilder.v1()
                .withId(UUID.randomUUID().toString())
                .withSource(URI.create("/cloudEvents"))
                .withType(CloudEventsService.class.getName())
                .withDataContentType("application/json")
                .withExtension("traceparent", Objects.requireNonNull(headers.get("traceparent")).get(0))
                .withExtension("tracestate", Objects.requireNonNull(headers.get("tracestate")).get(0))
                .build();

        CloudEvent outputEvent = CloudEventBuilder.from(cloudEvent)
                .withData(PojoCloudEventData.wrap(customerManagementItem, objectMapper::writeValueAsBytes))
                .build();

        return ResponseEntity.ok().headers(headers).body(outputEvent);

    }

}