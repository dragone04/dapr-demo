package com.spring_performance_a.services;

import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class HttpService {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpService.class);

    private final OkHttpClient client;

    public HttpService() {

        this.client = new OkHttpClient.Builder()
                .readTimeout(10, TimeUnit.SECONDS)
                .retryOnConnectionFailure(false)
                .connectionPool(new ConnectionPool(20, 5, TimeUnit.SECONDS))
                .build();

    }

    public int makeHttpCall(Request request) {

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                LOGGER.info("Response body: {}", response.body().string());
            }
            LOGGER.info("Response code: {}", response.code());
            return response.code();
        } catch (Exception e) {
            LOGGER.error("Error making HTTP call", e);
            return 500;
        }

    }

}
