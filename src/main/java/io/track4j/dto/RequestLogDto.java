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

    public static RequestLogDto fromExternalRequest(String url, String method,
                                                    String traceId, String spanId) {
        RequestLogDto dto = new RequestLogDto();
        dto.traceId = traceId;
        dto.spanId = spanId;
        dto.operationName = HTTP + " " + method + " " + extractHostFromUrl(url);
        dto.requestType = RequestLog.RequestType.EXTERNAL;
        dto.method = method;
        dto.url = url;
        dto.startTime = LocalDateTime.now();
        return dto;
    }

    public static RequestLogDto fromInternalCall(String className, String methodName,
                                                 String traceId, String spanId) {
        RequestLogDto dto = new RequestLogDto();
        dto.traceId = traceId;
        dto.spanId = spanId;
        dto.operationName = className + "." + methodName;
        dto.requestType = RequestLog.RequestType.INTERNAL;
        dto.method = RequestLog.RequestType.INTERNAL.getValue();
        dto.url = className + "." + methodName;
        dto.startTime = LocalDateTime.now();
        return dto;
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