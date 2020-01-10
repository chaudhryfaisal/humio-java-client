package com.github.chaudhryfaisal;

import com.github.chaudhryfaisal.dto.EventPayload;
import com.jsoniter.output.JsonStream;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

/**
 * Implementation of a Humio Ingest API.
 */
@Builder
@Data
public class Humio {
    public static final String DEFAULT_HOST_NAME = "https://cloud.humio.com";
    public static final String DEFAULT_PATH = "/api/v1/ingest/humio-structured";

    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    @Builder.Default
    private String endpoint = DEFAULT_HOST_NAME;
    @Builder.Default
    private String uri = DEFAULT_PATH;
    private String token;
    @Singular
    private Map<String, Object> tags;
    @Builder.Default
    private OkHttpClient client = new OkHttpClient.Builder().addInterceptor(new GzipRequestInterceptor()).build();

    public void write(final EventPayload eventPayload) {
        String json = JsonStream.serialize(Collections.singleton(eventPayload));
        RequestBody body = RequestBody.create(json, JSON);
        Request request = new Request.Builder()
                .url(endpoint + uri)
                .post(body)
                .addHeader("Authorization", "Bearer " + token)
                .build();
        try {
            client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void close() {
        if (this.client != null) {
            this.client.dispatcher().executorService().shutdown();
            this.client.connectionPool().evictAll();
        }
    }
}
