package io.track4j.objects;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class RequestLog {

    private static final ConcurrentMap<String, String> METHOD_CACHE = new ConcurrentHashMap<>();
    private static final String[] COMMON_METHODS = {"GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS"};

    static {
        for (String method : COMMON_METHODS) {
            METHOD_CACHE.put(method, method.intern());
        }
    }

    private String traceId;
    private String spanId;
    private String parentSpanId;
    private String operationName;
    private RequestType requestType;
    private String method;
    private String url;
    private String requestHeaders;
    private String requestBody;
    private String responseHeaders;
    private String responseBody;
    private int statusCode;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private long durationMs;
    private boolean success;
    private String errorMessage;
    private String userId;
    private String clientIp;
    private String tags;

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getSpanId() {
        return spanId;
    }

    public void setSpanId(String spanId) {
        this.spanId = spanId;
    }

    public String getParentSpanId() {
        return parentSpanId;
    }

    public void setParentSpanId(String parentSpanId) {
        this.parentSpanId = parentSpanId;
    }

    public String getOperationName() {
        return operationName;
    }

    public void setOperationName(String operationName) {
        this.operationName = operationName;
    }

    public RequestType getRequestType() {
        return requestType;
    }

    public void setRequestType(RequestType requestType) {
        this.requestType = requestType;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        if (method != null && METHOD_CACHE.containsKey(method)) {
            this.method = METHOD_CACHE.get(method);
        } else {
            this.method = method;
        }
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getRequestHeaders() {
        return this.requestHeaders;
    }

    public void setRequestHeaders(String headers) {
        this.requestHeaders = headers;
    }

    public String getRequestBody() {
        return this.requestBody;
    }

    public void setRequestBody(String body) {
        this.requestBody = body;
    }

    public String getResponseHeaders() {
        return this.responseHeaders;
    }

    public void setResponseHeaders(String headers) {
        this.responseHeaders = headers;
    }

    public String getResponseBody() {
        return this.responseBody;
    }

    public void setResponseBody(String body) {
        this.responseBody = body;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(long durationMs) {
        this.durationMs = durationMs;
    }

    public boolean getSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }
}