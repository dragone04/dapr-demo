package com.spring_producer.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring_producer.model.Message;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

@RestController
public class SecretService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecretService.class);

    @Value("${dapr.secretStore.endpoint}")
    private String secretStoreEndpoint;

    private static OkHttpClient client;

    public SecretService() {
        client = new OkHttpClient();
    }

    @PostMapping("/getSecret")
    public ResponseEntity<Message> getSecret(
            @RequestHeader("traceparent") String traceparent,
            @RequestHeader("tracestate") String tracestate,
            @RequestBody Message message
    ) {

        Request request = new Request.Builder()
                .header("Content-Type", "application/json")
                .addHeader("traceparent", traceparent)
                .addHeader("tracestate", tracestate)
                .url(secretStoreEndpoint)
                .build();

        try (Response response = client.newCall(request).execute()) {

            if (!response.isSuccessful()) {
                return new ResponseEntity<>(null, HttpStatus.SERVICE_UNAVAILABLE);
            }

            ObjectMapper mapper = new ObjectMapper();
            Map<String, String> secretMap = mapper.readValue(Objects.requireNonNull(response.body()).string(), Map.class);

            LOGGER.info("key: {}", message.key);
            LOGGER.info("Secrets: {}", secretMap);

            return new ResponseEntity<>(new Message(message.key, secretMap.get(message.key)), HttpStatus.OK);

        } catch (IOException e) {
            LOGGER.error("Error getting secret from secret store", e);
            return new ResponseEntity<>(null, HttpStatus.SERVICE_UNAVAILABLE);
        }

    }

}
