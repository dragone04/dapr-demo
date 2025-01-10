package com.spring_store.services;

import com.spring_store.model.Message;
import io.dapr.client.domain.CloudEvent;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import org.json.JSONObject;

@RestController
public class StoreService {

    private static final Logger LOGGER = LoggerFactory.getLogger(StoreService.class);

    @Value("${dapr.pubSub.storeEndpoint}")
    private String storeEndpoint;

    private final OkHttpClient client;

    public StoreService() {
        this.client = new OkHttpClient();
    }


    @PostMapping(path = "saveCloudEvent", consumes = MediaType.ALL_VALUE)
    public void saveCloudEvent(
            @RequestHeader("traceparent") String traceparent,
            @RequestHeader("tracestate") String tracestate,
            @RequestBody CloudEvent<Message> cloudEvent
    ) {

        LOGGER.info("traceparent: {}", traceparent);
        LOGGER.info("tracestate: {}", tracestate);
        LOGGER.info("Received message: {}", cloudEvent.getData());


        JSONObject data = new JSONObject();
        data.put( cloudEvent.getData().getKey(), cloudEvent.getData().getValue());

        String sqlText = String.format(
                "insert into public.cloud_event (id, source, type, specversion, datacontenttype, data) " +
                        "values ('%s', '%s', '%s', '%s', '%s', '%s');",
                cloudEvent.getId(),
                cloudEvent.getSource(),
                cloudEvent.getType(),
                cloudEvent.getSpecversion(),
                cloudEvent.getDatacontenttype(),
                data);

        LOGGER.info("sqlText: {}", sqlText);

        JSONObject command = new JSONObject();

        command.put("sql", sqlText);

        JSONObject payload = new JSONObject();
        payload.put("metadata", command);
        payload.put("operation", "exec");

        LOGGER.info("storeEndpoint: {}", storeEndpoint);


        LOGGER.info("payload: {}", payload);

        Request request = new Request.Builder()
                .header("Content-Type", "application/json")
                .addHeader("traceparent", traceparent)
                .addHeader("tracestate", tracestate)
                .url(storeEndpoint)
                .post(okhttp3.RequestBody.create(payload.toString().getBytes()))
                .build();

        try (Response response = client.newCall(request).execute()) {

            if (!response.isSuccessful()) {
                LOGGER.error("Error storing cloud event: {}", response.body());
                return;
            }

        } catch (Exception e) {
            LOGGER.error("Error storing cloud event", e);
        }

    }

}
