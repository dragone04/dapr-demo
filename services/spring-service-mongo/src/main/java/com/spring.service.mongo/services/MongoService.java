package com.spring.service.mongo.services;

import com.spring.service.mongo.repository.MessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MongoService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoService.class);

    private final MessageRepository messageRepository;

    public MongoService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @PostMapping(path = "/save", consumes = MediaType.ALL_VALUE)
    public ResponseEntity<com.spring.service.mongo.model.Message> save(
            @RequestHeader(value = "traceparent", required = false) String traceparent,
            @RequestHeader(value = "tracestate", required = false) String tracestate,
            @RequestBody(required = false) com.spring.service.mongo.model.Message message
    ) {
        LOGGER.info("traceparent: {}", traceparent);
        LOGGER.info("tracestate: {}", tracestate);

        messageRepository.save(message);

        LOGGER.info("message saved: {}", message);

        return ResponseEntity.ok(message);

    }

}
