package io.track4j.dto;

import io.track4j.entity.RequestLog;

import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;

public class RequestLogDto {
    private static final String HTTP = "HTTP";
    private String traceId;
    private String spanId;
    private String parentSpanId;
    private String operationName;
    private RequestLog.RequestType requestType;
    private String method;
    private String url;
    private String requestHeaders;
    private String requestBody;
    private String responseHeaders;
    private String responseBody;
    private Integer statusCode;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long durationMs;
    private Boolean success;
    private String errorMessage;
    private String userId;
    private String clientIp;
    private String tags;

    public RequestLogDto() {}

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private RequestLogDto dto = new RequestLogDto();

        public Builder traceId(String traceId) {
            dto.traceId = traceId;
            return this;
        }

        public Builder spanId(String spanId) {
            dto.spanId = spanId;
            return this;
        }

        public Builder parentSpanId(String parentSpanId) {
            dto.parentSpanId = parentSpanId;
            return this;
        }

        public Builder operationName(String operationName) {
            dto.operationName = operationName;
            return this;
        }

        public Builder requestType(RequestLog.RequestType requestType) {
            dto.requestType = requestType;
            return this;
        }

        public Builder method(String method) {
            dto.method = method;
            return this;
        }

        public Builder url(String url) {
            dto.url = url;
            return this;
        }

        public Builder requestHeaders(String requestHeaders) {
            dto.requestHeaders = requestHeaders;
            return this;
        }

        public Builder requestBody(String requestBody) {
            dto.requestBody = requestBody;
            return this;
        }

        public Builder responseHeaders(String responseHeaders) {
            dto.responseHeaders = responseHeaders;
            return this;
        }

        public Builder responseBody(String responseBody) {
            dto.responseBody = responseBody;
            return this;
        }

        public Builder statusCode(Integer statusCode) {
            dto.statusCode = statusCode;
            return this;
        }

        public Builder startTime(LocalDateTime startTime) {
            dto.startTime = startTime;
            return this;
        }

        public Builder endTime(LocalDateTime endTime) {
            dto.endTime = endTime;
            return this;
        }

        public Builder durationMs(Long durationMs) {
            dto.durationMs = durationMs;
            return this;
        }

        public Builder success(Boolean success) {
            dto.success = success;
            return this;
        }

        public Builder errorMessage(String errorMessage) {
            dto.errorMessage = errorMessage;
            return this;
        }

        public Builder userId(String userId) {
            dto.userId = userId;
            return this;
        }

        public Builder clientIp(String clientIp) {
            dto.clientIp = clientIp;
            return this;
        }

        public Builder tags(String tags) {
            dto.tags = tags;
            return this;
        }

        public RequestLogDto build() {
            return dto;
        }
    }

    public static RequestLogDto fromExternalRequest(String url, String method,
                                                    String traceId, String spanId) {
        return builder()
                .traceId(traceId)
                .spanId(spanId)
                .operationName(RequestLogDto.HTTP + " " + method + " " + extractHostFromUrl(url))
                .requestType(RequestLog.RequestType.EXTERNAL)
                .method(method)
                .url(url)
                .startTime(LocalDateTime.now())
                .build();
    }

    public static RequestLogDto fromInternalCall(String className, String methodName,
                                                 String traceId, String spanId) {
        return builder()
                .traceId(traceId)
                .spanId(spanId)
                .operationName(className + "." + methodName)
                .requestType(RequestLog.RequestType.INTERNAL)
                .method(RequestLog.RequestType.INTERNAL.getValue())
                .url(className + "." + methodName)
                .startTime(LocalDateTime.now())
                .build();
    }

    private static String extractHostFromUrl(String url) {
        try {
            return new URL(url).getHost();
        } catch (Exception e) {
            return url;
        }
    }

    public RequestLogDto completeWithResponse(Integer statusCode,
                                              String responseHeaders,
                                              String responseBody,
                                              boolean success,
                                              String errorMessage) {
        this.statusCode = statusCode;
        this.responseHeaders = responseHeaders;
        if (responseBody != null) {
            this.responseBody = responseBody;
        }
        this.success = success;
        this.errorMessage = errorMessage;
        this.endTime = LocalDateTime.now();

        if (startTime != null) {
            this.durationMs = Duration.between(startTime, endTime).toMillis();
        }

        return this;
    }

    public String getTraceId() { return traceId; }
    public void setTraceId(String traceId) { this.traceId = traceId; }

    public String getSpanId() { return spanId; }
    public void setSpanId(String spanId) { this.spanId = spanId; }

    public String getParentSpanId() { return parentSpanId; }
    public void setParentSpanId(String parentSpanId) { this.parentSpanId = parentSpanId; }

    public String getOperationName() { return operationName; }
    public void setOperationName(String operationName) { this.operationName = operationName; }

    public RequestLog.RequestType getRequestType() { return requestType; }
    public void setRequestType(RequestLog.RequestType requestType) { this.requestType = requestType; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getRequestHeaders() { return requestHeaders; }
    public void setRequestHeaders(String requestHeaders) { this.requestHeaders = requestHeaders; }

    public String getRequestBody() { return requestBody; }
    public void setRequestBody(String requestBody) { this.requestBody = requestBody; }

    public String getResponseHeaders() { return responseHeaders; }
    public void setResponseHeaders(String responseHeaders) { this.responseHeaders = responseHeaders; }

    public String getResponseBody() { return responseBody; }
    public void setResponseBody(String responseBody) { this.responseBody = responseBody; }

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