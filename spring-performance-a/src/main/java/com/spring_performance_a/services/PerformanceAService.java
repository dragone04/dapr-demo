package com.spring_performance_a.services;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring_performance_a.model.Message;
import okhttp3.Request;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PerformanceAService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PerformanceAService.class);

    @Value("${dapr.service.serviceEndpoint}")
    private String serviceEndpoint;

    @Value("${dapr.service.daprServiceEndpoint}")
    private String daprServiceEndpoint;


    private final HttpService httpService;

    public PerformanceAService(HttpService httpService) {
        this.httpService = httpService;
    }

    @PostMapping(path = "/callback", consumes = MediaType.ALL_VALUE)
    public ResponseEntity<Integer> callback(
            @RequestHeader("traceparent") String traceparent,
            @RequestHeader("tracestate") String tracestate,
            @RequestBody(required = false) Message message
    ) {

        LOGGER.info("Received traceparent: {}", traceparent);
        LOGGER.info("Received tracestate: {}", tracestate);
        LOGGER.info("Received message: {}", message);

        ObjectMapper mapper = new ObjectMapper();
        String mappedObject = null;
        int result = 404;

        try {
            mappedObject = mapper.writeValueAsString(message);
        } catch (JsonGenerationException | JsonMappingException e) {
            LOGGER.error("Error serializing message", e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        if (mappedObject != null) {
            // Call service B
            Request request = new Request.Builder()
                    .header("Content-Type", "application/json")
                    .addHeader("traceparent", traceparent)
                    .addHeader("tracestate", tracestate)
                    .url(daprServiceEndpoint)
                    .post(okhttp3.RequestBody.create(mappedObject.getBytes()))
                    .build();
            result = this.httpService.makeHttpCall(request);
        }

        return ResponseEntity.ok(result);

    }

}
