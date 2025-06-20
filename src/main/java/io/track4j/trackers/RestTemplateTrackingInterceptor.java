package io.track4j.trackers;

import io.track4j.autoconfigure.Track4jServiceManager;
import io.track4j.objects.context.TraceContext;
import io.track4j.objects.LightWeightClientHttpResponse;
import io.track4j.objects.RequestLog;
import io.track4j.objects.RequestType;
import io.track4j.properties.Track4jProperties;
import io.track4j.service.RequestLogService;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;

public class RestTemplateTrackingInterceptor implements ClientHttpRequestInterceptor {

    private final RequestLogService requestLogService;
    private final Track4jProperties properties;

    public RestTemplateTrackingInterceptor(RequestLogService requestLogService) {
        this.requestLogService = requestLogService;
        this.properties = Track4jServiceManager.getInstance().getProperties();
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                        ClientHttpRequestExecution execution) throws IOException {

        TraceContext.TraceInfo currentTrace = TraceContext.getTraceInfo();

        String traceId = currentTrace.traceId;
        if (traceId == null || traceId.isBlank()) {
            traceId = TraceContext.generateTraceId();
        }

        String parentSpanId = currentTrace.spanId;
        String spanId = TraceContext.generateSpanId();

        request.getHeaders().set(TraceContext.getTraceIdHeader(), traceId);
        request.getHeaders().set(TraceContext.getSpanIdHeader(), spanId);

        String url = request.getURI().toString();
        String method = request.getMethod().toString();
        LocalDateTime startTime = LocalDateTime.now();

        RequestLog requestLog = new RequestLog();
        requestLog.setTraceId(traceId);
        requestLog.setSpanId(spanId);
        requestLog.setParentSpanId(parentSpanId);
        requestLog.setOperationName("HTTP " + method + " " + extractHostFromUrl(url));
        requestLog.setRequestType(RequestType.EXTERNAL);
        requestLog.setMethod(method);
        requestLog.setUrl(url);
        if (properties.isIncludeHeaders()) {
            requestLog.setRequestHeaders(request.getHeaders().toString());
        }
        if (properties.isIncludeRequestBody() && body.length > 0) {
            requestLog.setRequestBody(new String(body, StandardCharsets.UTF_8));
        }

        requestLog.setStartTime(startTime);

        LightWeightClientHttpResponse bufferingResponse = null;
        Exception caughtException = null;

        try (ClientHttpResponse originalResponse = execution.execute(request, body)) {
            bufferingResponse = new LightWeightClientHttpResponse(originalResponse);

            LocalDateTime endTime = LocalDateTime.now();
            long durationMs = Duration.between(startTime, endTime).toMillis();
            int statusCode = bufferingResponse.getStatusCode().value();

            requestLog.setEndTime(endTime);
            requestLog.setDurationMs(durationMs);
            requestLog.setStatusCode(statusCode);
            requestLog.setResponseHeaders(bufferingResponse.getHeaders().toString());
            requestLog.setResponseBody(bufferingResponse.getBodyAsString());
            requestLog.setSuccess(bufferingResponse.getStatusCode().is2xxSuccessful());
            requestLog.setErrorMessage(null);

            return bufferingResponse;

        } catch (Exception e) {
            caughtException = e;

            LocalDateTime endTime = LocalDateTime.now();
            long durationMs = Duration.between(startTime, endTime).toMillis();

            requestLog.setEndTime(endTime);
            requestLog.setDurationMs(durationMs);
            requestLog.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            requestLog.setResponseHeaders(null);
            requestLog.setResponseBody(null);
            requestLog.setSuccess(false);
            requestLog.setErrorMessage(e.getMessage());

            return null;
        } finally {
            requestLogService.logRequestAsync(requestLog);
            if (bufferingResponse != null && caughtException != null) {
                bufferingResponse.close();
            }
        }
    }

    private String extractHostFromUrl(String url) {
        try {
            int start = url.indexOf("://");
            if (start != -1) {
                start += 3;
                int end = url.indexOf('/', start);
                if (end == -1) {
                    end = url.indexOf('?', start);
                }
                if (end == -1) {
                    end = url.length();
                }
                return url.substring(start, end);
            }
            return url;
        } catch (Exception e) {
            return url;
        }
    }
}