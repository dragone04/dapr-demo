package com.spring_store.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;


@RestController
public class HealthService {

    private static final Logger LOGGER = LoggerFactory.getLogger(HealthService.class);


    @GetMapping("/api/health")
    public ResponseEntity<String> getMethodName() {
        LOGGER.info("Health check");
        return new ResponseEntity<>("OK", HttpStatus.OK);

    }

}
