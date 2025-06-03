package io.track4j.context;

import org.slf4j.MDC;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;

public final class TraceContext {
    private static final String TRACE_ID_HEADER = "X-Trace-ID";
    private static final String SPAN_ID_HEADER = "X-Span-ID";
    private static final ThreadLocal<TraceData> CONTEXT = ThreadLocal.withInitial(TraceData::new);
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Base64.Encoder BASE64_ENCODER = Base64.getUrlEncoder().withoutPadding();

    private TraceContext() {
    }

    private static class TraceData {
        String traceId;
        String spanId;

        void clear() {
            traceId = null;
            spanId = null;
        }
    }

    public static String getTraceId() {
        return CONTEXT.get().traceId;
    }

    public static String getSpanId() {
        return CONTEXT.get().spanId;
    }

    public static void setTraceId(String id) {
        TraceData data = CONTEXT.get();
        data.traceId = id;
        MDC.put("traceId", id);
    }

    public static void setSpanId(String id) {
        TraceData data = CONTEXT.get();
        data.spanId = id;
        MDC.put("spanId", id);
    }

    public static TraceInfo getTraceInfo() {
        TraceData data = CONTEXT.get();
        return new TraceInfo(data.traceId, data.spanId);
    }

    public static class TraceInfo {
        public final String traceId;
        public final String spanId;

        public TraceInfo(String traceId, String spanId) {
            this.traceId = traceId;
            this.spanId = spanId;
        }
    }


    public static void setTraceData(String traceId, String spanId) {
        TraceData data = CONTEXT.get();
        data.traceId = traceId;
        data.spanId = spanId;

        if (traceId != null) {
            MDC.put("traceId", traceId);
        }
        if (spanId != null) {
            MDC.put("spanId", spanId);
        }
    }

    public static String generateTraceId() {
        UUID uuid = UUID.randomUUID();
        long msb = uuid.getMostSignificantBits();
        long lsb = uuid.getLeastSignificantBits();

        StringBuilder sb = new StringBuilder(32);
        appendHex(sb, msb >>> 32);
        appendHex(sb, msb);
        appendHex(sb, lsb >>> 32);
        appendHex(sb, lsb);

        return sb.toString();
    }

    public static String generateSpanId() {
        byte[] bytes = new byte[8];
        RANDOM.nextBytes(bytes);
        return BASE64_ENCODER.encodeToString(bytes);
    }

    public static void clear() {
        TraceData data = CONTEXT.get();
        data.clear();
        CONTEXT.remove();
        MDC.clear();
    }

    public static String getTraceIdHeader() {
        return TRACE_ID_HEADER;
    }

    public static String getSpanIdHeader() {
        return SPAN_ID_HEADER;
    }

    private static void appendHex(StringBuilder sb, long value) {
        String hex = Long.toHexString(value);
        for (int i = hex.length(); i < 8; i++) {
            sb.append('0');
        }
        sb.append(hex);
    }
}
