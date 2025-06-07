package io.track4j.objects;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.FastByteArrayOutputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class LightWeightClientHttpResponse implements ClientHttpResponse {
    private final ClientHttpResponse response;
    private byte[] body;
    private volatile boolean bodyCached = false;
    private static final int BUFFER_SIZE = 8192;
    private static final ThreadLocal<byte[]> BUFFER_POOL = ThreadLocal.withInitial(() -> new byte[BUFFER_SIZE]);

    public LightWeightClientHttpResponse(ClientHttpResponse response) {
        this.response = response;
    }

    @Override
    public HttpStatus getStatusCode() throws IOException {
        return HttpStatus.valueOf(response.getStatusCode().value());
    }

    @Override
    public String getStatusText() throws IOException {
        return response.getStatusText();
    }

    @Override
    public void close() {
        response.close();
    }

    @Override
    public InputStream getBody() throws IOException {
        ensureBodyCached();
        return new ByteArrayInputStream(body);
    }

    @Override
    public HttpHeaders getHeaders() {
        return response.getHeaders();
    }

    public String getBodyAsString() {
        try {
            ensureBodyCached();
            return body.length > 0 ? new String(body, StandardCharsets.UTF_8) : "";
        } catch (IOException e) {
            return null;
        }
    }

    private void ensureBodyCached() throws IOException {
        if (!bodyCached) {
            synchronized (this) {
                if (!bodyCached) {
                    body = copyToByteArray(response.getBody());
                    bodyCached = true;
                }
            }
        }
    }

    private static byte[] copyToByteArray(InputStream in) throws IOException {
        try (FastByteArrayOutputStream fbaOutputStream = new FastByteArrayOutputStream(BUFFER_SIZE)) {
            byte[] buffer = BUFFER_POOL.get();
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                fbaOutputStream.write(buffer, 0, bytesRead);
            }
            return fbaOutputStream.toByteArray();
        }
    }
}