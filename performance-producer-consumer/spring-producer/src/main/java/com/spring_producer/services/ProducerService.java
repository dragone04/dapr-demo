package com.spring_producer.services;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring_producer.model.CustomCloudEvent;
import com.spring_producer.model.Message;
import io.dapr.client.domain.CloudEvent;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@RestController
public class ProducerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProducerService.class);

    @Value("${dapr.pubSub.pubSubName}")
    private String pubSubName;

    @Value("${dapr.pubSub.topicName}")
    private String topicName;

    @Value("${dapr.pubSub.pubSubEndpoint}")
    private String pubSubEndpoint;

    private final SecretService secretService;

    private final OkHttpClient client;

    public ProducerService(SecretService secretService) {
        this.secretService = secretService;
        this.client = new OkHttpClient();
    }

    @PostMapping("/produce")
    public ResponseEntity<String> produce(
            @RequestHeader("traceparent") String traceparent,
            @RequestHeader("tracestate") String tracestate,
            @RequestBody Message message
    ) {

        LOGGER.info("traceparent: {}", traceparent);
        LOGGER.info("tracestate: {}", tracestate);
        LOGGER.info("message: {}", message);

        ResponseEntity<Message> result = this.secretService.getSecret(traceparent, tracestate, message);

        if (result != null && result.getBody() != null && result.getBody().key != null) {

            LOGGER.info("key: {}", result.getBody().key);

            CustomCloudEvent<Message> cloudEvent2 = new CustomCloudEvent<>();
            cloudEvent2.setId(UUID.randomUUID().toString());
            cloudEvent2.setSource("spring-producer");
            cloudEvent2.setType("com.dapr.cloudevent.sent");
            cloudEvent2.setSpecversion("1.0");
            cloudEvent2.setDatacontenttype(CloudEvent.CONTENT_TYPE);
            cloudEvent2.setData(result.getBody());
            cloudEvent2.setTraceparent(traceparent);
            cloudEvent2.setTracestate(tracestate);
            cloudEvent2.setTopic(topicName);
            cloudEvent2.setPubsubname(pubSubName);
            cloudEvent2.setTraceid(traceparent);
            cloudEvent2.setTime(OffsetDateTime.now(ZoneOffset.of("+02:00")));
            cloudEvent2.setSubject("cloud message");

            ObjectMapper mapper = new ObjectMapper();
            String mappedObject = null;

            try {
                mappedObject = mapper.writeValueAsString(cloudEvent2);
            } catch (JsonGenerationException | JsonMappingException e) {
                LOGGER.error("Error serializing message", e);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

            Request request;

            if (mappedObject != null) {

                request = new Request.Builder()
                        .header("Content-Type", "application/cloudevents+json")
                        .addHeader("traceparent", traceparent)
                        .addHeader("tracestate", tracestate)
                        .url(pubSubEndpoint)
                        .post(okhttp3.RequestBody.create(mappedObject.getBytes()))
                        .build();

                try (Response response = client.newCall(request).execute()) {

                    if (!response.isSuccessful()) {
                        LOGGER.error("Error publishing message, not successful");
                        return new ResponseEntity<>("Error publishing message, not successful", HttpStatus.INTERNAL_SERVER_ERROR);
                    }

                } catch (Exception e) {
                    LOGGER.error("Error publishing message", e);
                    return new ResponseEntity<>("Error publishing message", HttpStatus.INTERNAL_SERVER_ERROR);
                }

            }

        } else {
            LOGGER.error("Error getting secret from secret store");
            return new ResponseEntity<>("Error getting secret from secret store", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>("Message published!", HttpStatus.OK);

    }

}
