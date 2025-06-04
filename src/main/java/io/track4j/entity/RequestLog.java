package io.track4j.entity;

import java.lang.ref.SoftReference;
import java.time.LocalDateTime;

public class RequestLog {
    private String traceId;
    private String spanId;
    private String parentSpanId;
    private String operationName;
    private RequestType requestType;
    private String method;
    private String url;
    private SoftReference<String> requestHeaders;
    private SoftReference<String> requestBody;
    private SoftReference<String> responseHeaders;
    private SoftReference<String> responseBody;
    private Integer statusCode;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long durationMs;
    private Boolean success;
    private String errorMessage;
    private String userId;
    private String clientIp;
    private String tags;

    public String getTraceId() { return traceId; }
    public void setTraceId(String traceId) { this.traceId = traceId; }

    public String getSpanId() { return spanId; }
    public void setSpanId(String spanId) { this.spanId = spanId; }

    public String getParentSpanId() { return parentSpanId; }
    public void setParentSpanId(String parentSpanId) { this.parentSpanId = parentSpanId; }

    public String getOperationName() { return operationName; }
    public void setOperationName(String operationName) { this.operationName = operationName; }

    public RequestType getRequestType() { return requestType; }
    public void setRequestType(RequestType requestType) { this.requestType = requestType; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getRequestHeaders() {
        return requestHeaders != null ? requestHeaders.get() : null;
    }

    public void setRequestHeaders(String headers) {
        this.requestHeaders = headers != null ? new SoftReference<>(headers) : null;
    }

    public String getRequestBody() {
        return requestBody != null ? requestBody.get() : null;
    }

    public void setRequestBody(String body) {
        this.requestBody = body != null ? new SoftReference<>(body) : null;
    }

    public String getResponseHeaders() {
        return responseHeaders != null ? responseHeaders.get() : null;
    }

    public void setResponseHeaders(String headers) {
        this.responseHeaders = headers != null ? new SoftReference<>(headers) : null;
    }

    public String getResponseBody() {
        return responseBody != null ? responseBody.get() : null;
    }

    public void setResponseBody(String body) {
        this.responseBody = body != null ? new SoftReference<>(body) : null;
    }

    public Integer getStatusCode() { return statusCode; }
    public void setStatusCode(Integer statusCode) { this.statusCode = statusCode; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public Long getDurationMs() { return durationMs; }
    public void setDurationMs(Long durationMs) { this.durationMs = durationMs; }

    public Boolean getSuccess() { return success; }
    public void setSuccess(Boolean success) { this.success = success; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getClientIp() { return clientIp; }
    public void setClientIp(String clientIp) { this.clientIp = clientIp; }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
}