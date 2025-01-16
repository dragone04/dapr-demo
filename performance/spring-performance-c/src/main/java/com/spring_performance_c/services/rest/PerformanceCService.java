package com.spring_performance_c.services.rest;

import com.spring_performance_c.model.Message;
import com.spring_performance_c.service.MessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PerformanceCService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PerformanceCService.class);

    private final MessageRepository messageRepository;

    public PerformanceCService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @PostMapping(path = "/save", consumes = MediaType.ALL_VALUE)
    public ResponseEntity<Message> save(
            @RequestBody(required = false) Message message
    ) {

        messageRepository.save(message);
        LOGGER.info("Message saved: {}", message);

        return ResponseEntity.ok(message);

    }

}
