package com.spring_consumer.services;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring_consumer.model.CustomCloudEvent;
import io.dapr.Topic;
import okhttp3.Request;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ConsumeService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConsumeService.class);

    @Value("${dapr.service.storeServiceEndpoint}")
    private String storeServiceEndpoint;

    @Value("${dapr.service.stateStoreEndpoint}")
    private String stateStoreEndpoint;

    static final String TOPIC_NAME = "dapr";
    static final String PUB_SUB_NAME = "pubsub";
    static final String STATE = "state";

    private final HttpService httpService;

    public ConsumeService(HttpService httpService) {
        this.httpService = httpService;
    }

    @PostMapping(path = "/consume", consumes = MediaType.ALL_VALUE)
    @Topic(name = TOPIC_NAME, pubsubName = PUB_SUB_NAME)
    public void consume(
            @RequestHeader("traceparent") String traceparent,
            @RequestBody(required = false) CustomCloudEvent<?> message
    ) {

        LOGGER.info("traceparent: {}", traceparent);
        ObjectMapper mapper = new ObjectMapper();
        String mappedObject = null;

        try {
            mappedObject = mapper.writeValueAsString(message);
        } catch (JsonGenerationException | JsonMappingException e) {
            LOGGER.error("Error serializing message", e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        if (mappedObject != null) {

            JSONArray array = new JSONArray();
            JSONObject data = new JSONObject();
            data.put("key", STATE);
            data.put("value", 1);
            array.put(data);

            Request stateStorePreStore = new Request.Builder()
                    .header("Content-Type", "application/json")
                    .addHeader("traceparent", traceparent)
                    .addHeader("tracestate", message.getTracestate())
                    .url(stateStoreEndpoint)
                    .post(okhttp3.RequestBody.create(array.toString().getBytes()))
                    .build();

            httpService.makeHttpCall(stateStorePreStore);

            Request storeServiceRequest = new Request.Builder()
                    .header("Content-Type", "application/json")
                    .addHeader("traceparent", message.getTraceparent())
                    .addHeader("tracestate", message.getTracestate())
                    .url(storeServiceEndpoint)
                    .post(okhttp3.RequestBody.create(mappedObject.getBytes()))
                    .build();

            int storeServiceResponse = httpService.makeHttpCall(storeServiceRequest);

            if (storeServiceResponse == 200) {
                array = new JSONArray();
                data.put("key", STATE);
                data.put("value", 1);
                array.put(data);
                Request stateStore = new Request.Builder()
                        .header("Content-Type", "application/json")
                        .addHeader("traceparent", message.getTraceparent())
                        .addHeader("tracestate", message.getTracestate())
                        .url(stateStoreEndpoint)
                        .post(okhttp3.RequestBody.create(array.toString().getBytes()))
                        .build();
                httpService.makeHttpCall(stateStore);
            }

        }

    }

}
