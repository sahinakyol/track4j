package io.track4j.context;

import org.slf4j.MDC;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;

public final class TraceContext {
    private static final String TRACE_ID_HEADER = "X-Trace-ID";
    private static final String SPAN_ID_HEADER = "X-Span-ID";
    private static final ThreadLocal<String> traceId = new ThreadLocal<>();
    private static final ThreadLocal<String> spanId = new ThreadLocal<>();
    private static final SecureRandom random = new SecureRandom();

    private TraceContext() {
    }

    public static String getTraceId() {
        return traceId.get();
    }

    public static String getSpanId() {
        return spanId.get();
    }

    public static void setTraceId(String id) {
        traceId.set(id);
        MDC.put("traceId", id);
    }

    public static void setSpanId(String id) {
        spanId.set(id);
        MDC.put("spanId", id);
    }

    public static String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static String generateSpanId() {
        byte[] bytes = new byte[8];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public static void clear() {
        traceId.remove();
        spanId.remove();
        MDC.clear();
    }

    public static String getTraceIdHeader() {
        return TRACE_ID_HEADER;
    }

    public static String getSpanIdHeader() {
        return SPAN_ID_HEADER;
    }
}
