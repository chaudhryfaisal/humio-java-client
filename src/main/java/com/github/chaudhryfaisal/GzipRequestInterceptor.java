package com.github.chaudhryfaisal;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;
import okio.GzipSink;
import okio.Okio;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * Implementation of a interceptor to compress http's body using GZIP.
 */
final class GzipRequestInterceptor implements Interceptor {

    @Override
    public Response intercept(final Chain chain) throws IOException {
        Request originalRequest = chain.request();
        RequestBody body = originalRequest.body();
        if (body == null || originalRequest.header("Content-Encoding") != null) {
            return chain.proceed(originalRequest);
        }

        Request compressedRequest = originalRequest.newBuilder().header("Content-Encoding", "gzip")
                .method(originalRequest.method(), gzip(body)).build();
        return chain.proceed(compressedRequest);
    }

    private RequestBody gzip(final RequestBody body) {
        return new RequestBody() {
            @Override
            public MediaType contentType() {
                return body.contentType();
            }

            @Override
            public long contentLength() {
                return -1; // We don't know the compressed length in advance!
            }

            @Override
            public void writeTo(@NotNull final BufferedSink sink) throws IOException {
                BufferedSink gzipSink = Okio.buffer(new GzipSink(sink));
                body.writeTo(gzipSink);
                gzipSink.close();
            }
        };
    }
}
