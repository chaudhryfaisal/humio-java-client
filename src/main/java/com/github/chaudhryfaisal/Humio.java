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
import okhttp3.Response;

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
    public static final int DEFAULT_HTTP_SUCCESS_CODE = 200;

    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    @Builder.Default
    private boolean debug = false;
    @Builder.Default
    private String endpoint = DEFAULT_HOST_NAME;
    @Builder.Default
    private String uri = DEFAULT_PATH;
    @Builder.Default
    private int successCode = DEFAULT_HTTP_SUCCESS_CODE;
    private String token;
    @Singular
    private Map<String, Object> tags;
    @Builder.Default
    private OkHttpClient client = new OkHttpClient.Builder().addInterceptor(new GzipRequestInterceptor()).build();

    public void write(final EventPayload eventPayload) {
        if (debug) {
            System.out.println("Humio:write eventPayload=" + eventPayload);
        }
        if (endpoint == null || endpoint.isEmpty()) {
            endpoint = DEFAULT_HOST_NAME;
        }
        if (uri == null || uri.isEmpty()) {
            uri = DEFAULT_PATH;
        }
        Request request = new Request.Builder()
                .url(endpoint + uri)
                .post(RequestBody.create(JsonStream.serialize(Collections.singleton(eventPayload)), JSON))
                .addHeader("Authorization", "Bearer " + token)
                .build();
        try {
            Response resp = client.newCall(request).execute();
            if (resp.body() != null) {
                resp.body().close();
            }
            if (resp.code() != successCode) {
                System.err.printf("Humio:WARN statusCode=%d failedEvents=%d\n", resp.code(), eventPayload.getEvents().size());
            }
            if (debug) {
                System.out.println("Humio:write resp=" + resp);
            }
        } catch (IOException e) {
            System.out.println("Humio:write " + e.getLocalizedMessage());
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
