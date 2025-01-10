package com.spring_cron.services;

import okhttp3.Request;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class CronServiceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CronServiceService.class);

    @Value("${dapr.pubSub.produceEndpoint}")
    private String produceEndpoint;

    private final HttpService httpService;

    public CronServiceService(HttpService httpService) {
        this.httpService = httpService;
    }

    @PostMapping("/cron")
    public ResponseEntity<String> cron(
            @RequestHeader("traceparent") String traceparent
    ) {

        LOGGER.info("traceparent: {}", traceparent);

        JSONObject data = new JSONObject();
        data.put("key", "secret");
        Request request = new Request.Builder()
                .header("Content-Type", "application/json")
                .addHeader("traceparent", traceparent)
                .addHeader("tracestate", UUID.randomUUID().toString())
                .url(produceEndpoint)
                .post(okhttp3.RequestBody.create(data.toString().getBytes()))
                .build();

        int responseCode = httpService.makeHttpCall(request);

        LOGGER.info("response: {}", responseCode);
        return new ResponseEntity<>("status code: " + responseCode, HttpStatus.OK);

    }

}
