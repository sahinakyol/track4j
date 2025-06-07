package io.track4j.objects;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import org.springframework.util.FastByteArrayOutputStream;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class LightweightResponseWrapper extends HttpServletResponseWrapper {
    private LightWeightServletOutputStream outputStream;
    private PrintWriter writer;
    private final FastByteArrayOutputStream fbaOutputStream = new FastByteArrayOutputStream();

    public LightweightResponseWrapper(HttpServletResponse response) {
        super(response);
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (outputStream == null) {
            outputStream = new LightWeightServletOutputStream(super.getOutputStream(), fbaOutputStream);
        }
        return outputStream;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (writer == null) {
            String encoding = getCharacterEncoding();
            writer = new PrintWriter(new OutputStreamWriter(getOutputStream(),
                    encoding != null ? Charset.forName(encoding) : StandardCharsets.UTF_8));
        }
        return writer;
    }

    public byte[] getContentAsByteArray() {
        return fbaOutputStream.toByteArray();
    }

    public void copyBodyToResponse() throws IOException {
        if (writer != null) {
            writer.flush();
        }
        if (outputStream != null) {
            outputStream.flush();
        }
    }

    private static class LightWeightServletOutputStream extends ServletOutputStream {
        private final ServletOutputStream original;
        private final FastByteArrayOutputStream buffer;

        LightWeightServletOutputStream(ServletOutputStream original, FastByteArrayOutputStream buffer) {
            this.original = original;
            this.buffer = buffer;
        }

        @Override
        public void write(int b) throws IOException {
            original.write(b);
            buffer.write(b);
        }

        @Override
        public void write(byte[] b) throws IOException {
            original.write(b);
            buffer.write(b);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            original.write(b, off, len);
            buffer.write(b, off, len);
        }

        @Override
        public void flush() throws IOException {
            original.flush();
        }

        @Override
        public void close() throws IOException {
            original.close();
        }

        @Override
        public boolean isReady() {
            return original.isReady();
        }

        @Override
        public void setWriteListener(WriteListener listener) {
            original.setWriteListener(listener);
        }
    }
}