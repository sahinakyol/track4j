package io.track4j.dto;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.util.FastByteArrayOutputStream;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class LightweightRequestWrapper extends HttpServletRequestWrapper {
    private byte[] cachedBody;
    private ServletInputStream inputStream;
    private BufferedReader reader;

    public LightweightRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);
        cacheRequestBody(request);
    }

    private void cacheRequestBody(HttpServletRequest request) throws IOException {
        try (InputStream requestInputStream = request.getInputStream();
             FastByteArrayOutputStream byteArrayOutputStream = new FastByteArrayOutputStream()) {
            
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = requestInputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }
            this.cachedBody = byteArrayOutputStream.toByteArray();
        }
    }

    @Override
    public ServletInputStream getInputStream() {
        if (inputStream == null) {
            inputStream = new CachedBodyServletInputStream(cachedBody);
        }
        return inputStream;
    }

    @Override
    public BufferedReader getReader() throws IOException {
        if (reader == null) {
            reader = new BufferedReader(new InputStreamReader(getInputStream(), getCharacterEncoding()));
        }
        return reader;
    }

    public byte[] getContentAsByteArray() {
        return cachedBody != null ? cachedBody : new byte[0];
    }

    public String getContentAsString() {
        if (cachedBody != null && cachedBody.length > 0) {
            return new String(cachedBody, StandardCharsets.UTF_8);
        }
        return "";
    }

    private static class CachedBodyServletInputStream extends ServletInputStream {
        private final ByteArrayInputStream inputStream;

        public CachedBodyServletInputStream(byte[] cachedBody) {
            this.inputStream = new ByteArrayInputStream(cachedBody != null ? cachedBody : new byte[0]);
        }

        @Override
        public int read() {
            return inputStream.read();
        }

        @Override
        public int read(byte[] b) throws IOException {
            return inputStream.read(b);
        }

        @Override
        public int read(byte[] b, int off, int len) {
            return inputStream.read(b, off, len);
        }

        @Override
        public boolean isFinished() {
            return inputStream.available() == 0;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(ReadListener listener) {
            // Not needed for cached body
        }
    }
}
