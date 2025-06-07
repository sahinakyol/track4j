package io.track4j.objects;

import io.track4j.helper.IntegerHelper;
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

public class LightweightRequestWrapper extends HttpServletRequestWrapper {
    private byte[] cachedBody;
    private ServletInputStream inputStream;
    private BufferedReader reader;
    private static final int BUFFER_SIZE = 8192;
    private static final ThreadLocal<byte[]> BUFFER_POOL = ThreadLocal.withInitial(() -> new byte[BUFFER_SIZE]);

    public LightweightRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);
        String contentLength = request.getHeader("Content-Length");
        if (contentLength != null && !"0".equals(contentLength)) {
            cacheRequestBody(request, IntegerHelper.parseInt(contentLength));
        } else {
            this.cachedBody = new byte[0];
        }
    }

    private void cacheRequestBody(HttpServletRequest request, int contentLength) throws IOException {
        try (InputStream requestInputStream = request.getInputStream();
             FastByteArrayOutputStream fbaOutputStream = new FastByteArrayOutputStream(contentLength)) {

            byte[] buffer = BUFFER_POOL.get();
            int bytesRead;
            while ((bytesRead = requestInputStream.read(buffer)) != -1) {
                fbaOutputStream.write(buffer, 0, bytesRead);
            }
            this.cachedBody = fbaOutputStream.toByteArray();
        }
    }

    @Override
    public ServletInputStream getInputStream() {
        if (inputStream == null) {
            inputStream = new LightWeightServletInputStream(cachedBody);
        }
        return inputStream;
    }

    @Override
    public BufferedReader getReader() throws IOException {
        if (reader == null) {
            reader = new BufferedReader(new InputStreamReader(getInputStream(), getCharacterEncoding()), BUFFER_SIZE);
        }
        return reader;
    }

    public byte[] getContentAsByteArray() {
        return cachedBody != null ? cachedBody : new byte[0];
    }

    private static class LightWeightServletInputStream extends ServletInputStream {
        private final ByteArrayInputStream inputStream;
        private final int totalBytes;
        private int bytesRead = 0;

        public LightWeightServletInputStream(byte[] cachedBody) {
            this.inputStream = new ByteArrayInputStream(cachedBody != null ? cachedBody : new byte[0]);
            this.totalBytes = cachedBody != null ? cachedBody.length : 0;
        }

        @Override
        public int read() {
            int result = inputStream.read();
            if (result != -1) {
                bytesRead++;
            }
            return result;
        }

        @Override
        public int read(byte[] b) throws IOException {
            int count = inputStream.read(b);
            if (count > 0) {
                bytesRead += count;
            }
            return count;
        }

        @Override
        public int read(byte[] b, int off, int len) {
            int count = inputStream.read(b, off, len);
            if (count > 0) {
                bytesRead += count;
            }
            return count;
        }

        @Override
        public boolean isFinished() {
            return bytesRead >= totalBytes;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(ReadListener listener) {
            // Not needed for cached body
        }

        @Override
        public int available() {
            return inputStream.available();
        }

        @Override
        public long skip(long n) {
            long skipped = inputStream.skip(n);
            bytesRead += (int) skipped;
            return skipped;
        }
    }
}
