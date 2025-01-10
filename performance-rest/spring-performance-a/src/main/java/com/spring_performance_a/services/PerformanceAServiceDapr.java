package com.spring_performance_a.services;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring_performance_a.model.Message;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
public class PerformanceAServiceDapr {

    private static final Logger LOGGER = LoggerFactory.getLogger(PerformanceAServiceDapr.class);

    @Value("${dapr.service.daprServiceEndpoint}")
    private String daprServiceEndpoint;

    private final HttpService httpService;

    public PerformanceAServiceDapr(HttpService httpService) {
        this.httpService = httpService;
    }

    @PostMapping(path = "/callbackDapr", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> callbackDapr(
            @RequestHeader("traceparent") String traceparent,
            @RequestHeader("tracestate") String tracestate,
            @RequestBody(required = false) Message message
    ) {

        LOGGER.info("Received traceparent: {}", traceparent);
        LOGGER.info("Received tracestate: {}", tracestate);
        LOGGER.info("Received message: {}", message);

        ObjectMapper mapper = new ObjectMapper();
        String mappedObject = null;
        String result;

        try {
            mappedObject = mapper.writeValueAsString(message);
        } catch (JsonGenerationException | JsonMappingException e) {
            LOGGER.error("Error serializing message", e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        if (mappedObject != null) {
            try {
                Request request = new Request.Builder()
                        .header("Content-Type", "application/json")
                        .addHeader("traceparent", traceparent)
                        .addHeader("tracestate", tracestate)
                        .url(daprServiceEndpoint)
                        .post(okhttp3.RequestBody.create(mappedObject.getBytes()))
                        .build();

                result = this.httpService.makeHttpCall(request);
                return ResponseEntity.ok(result);

            } catch (Exception e) {
                LOGGER.error("Error making HTTP call", e);
                return ResponseEntity.status(500).build();
            }
        } else {
            return ResponseEntity.status(500).build();
        }

    }

}
