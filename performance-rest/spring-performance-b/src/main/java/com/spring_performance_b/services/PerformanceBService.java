package com.spring_performance_b.services;

import com.spring_performance_b.model.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PerformanceBService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PerformanceBService.class);


    @PostMapping(path = "/callback", consumes = MediaType.ALL_VALUE)
    public ResponseEntity<Message> callback(
            @RequestHeader("traceparent") String traceparent,
            @RequestHeader("tracestate") String tracestate,
            @RequestBody(required = false) Message message
    ) {

        LOGGER.info("Received traceparent: {}", traceparent);
        LOGGER.info("Received tracestate: {}", tracestate);
        LOGGER.info("Received message: {}", message);

        return ResponseEntity.ok(message);

    }

}
