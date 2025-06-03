package io.track4j.dto;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import org.springframework.util.FastByteArrayOutputStream;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

public class LightweightResponseWrapper extends HttpServletResponseWrapper {
    private TeeServletOutputStream teeStream;
    private PrintWriter writer;
    private FastByteArrayOutputStream bodyBuffer = new FastByteArrayOutputStream();

    public LightweightResponseWrapper(HttpServletResponse response) {
        super(response);
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (teeStream == null) {
            teeStream = new TeeServletOutputStream(super.getOutputStream(), bodyBuffer);
        }
        return teeStream;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (writer == null) {
            writer = new PrintWriter(new OutputStreamWriter(getOutputStream(), getCharacterEncoding()));
        }
        return writer;
    }

    public byte[] getContentAsByteArray() {
        return bodyBuffer.toByteArray();
    }

    public String getContentAsString() {
        byte[] content = getContentAsByteArray();
        if (content.length > 0) {
            return new String(content, StandardCharsets.UTF_8);
        }
        return "";
    }

    public void copyBodyToResponse() throws IOException {
        if (writer != null) {
            writer.flush();
        }
        if (teeStream != null) {
            teeStream.flush();
        }
    }

    private static class TeeServletOutputStream extends ServletOutputStream {
        private final ServletOutputStream originalStream;
        private final FastByteArrayOutputStream buffer;

        public TeeServletOutputStream(ServletOutputStream originalStream, FastByteArrayOutputStream buffer) {
            this.originalStream = originalStream;
            this.buffer = buffer;
        }

        @Override
        public void write(int b) throws IOException {
            originalStream.write(b);
            buffer.write(b);
        }

        @Override
        public void write(byte[] b) throws IOException {
            originalStream.write(b);
            buffer.write(b);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            originalStream.write(b, off, len);
            buffer.write(b, off, len);
        }

        @Override
        public void flush() throws IOException {
            originalStream.flush();
        }

        @Override
        public void close() throws IOException {
            originalStream.close();
        }

        @Override
        public boolean isReady() {
            return originalStream.isReady();
        }

        @Override
        public void setWriteListener(WriteListener listener) {
            originalStream.setWriteListener(listener);
        }
    }
}