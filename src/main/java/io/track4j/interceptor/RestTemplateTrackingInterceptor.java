package io.track4j.interceptor;

import io.track4j.context.TraceContext;
import io.track4j.dto.RequestLogDto;
import io.track4j.service.RequestLogService;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicBoolean;

public class RestTemplateTrackingInterceptor implements ClientHttpRequestInterceptor {

    private final RequestLogService requestLogService;

    public RestTemplateTrackingInterceptor(RequestLogService requestLogService) {
        this.requestLogService = requestLogService;
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
        String requestHeaders = request.getHeaders().toString();
        String requestBody = body.length > 0 ? new String(body, StandardCharsets.UTF_8) : null;
        LocalDateTime startTime = LocalDateTime.now();

        RequestLogDto logDto = new RequestLogDto();
        logDto.setTraceId(traceId);
        logDto.setSpanId(spanId);
        logDto.setParentSpanId(parentSpanId);
        logDto.setOperationName("HTTP " + method + " " + extractHostFromUrl(url));
        logDto.setRequestType(io.track4j.entity.RequestLog.RequestType.EXTERNAL);
        logDto.setMethod(method);
        logDto.setUrl(url);
        logDto.setRequestHeaders(requestHeaders);
        logDto.setRequestBody(requestBody);
        logDto.setStartTime(startTime);



        BufferingClientHttpResponse bufferingResponse = null;
        Exception caughtException = null;

        try(ClientHttpResponse originalResponse = execution.execute(request, body)) {
            bufferingResponse = new BufferingClientHttpResponse(originalResponse);

            LocalDateTime endTime = LocalDateTime.now();
            long durationMs = Duration.between(startTime, endTime).toMillis();
            int statusCode = bufferingResponse.getStatusCode().value();

            logDto.setEndTime(endTime);
            logDto.setDurationMs(durationMs);
            logDto.setStatusCode(statusCode);
            logDto.setResponseHeaders(bufferingResponse.getHeaders().toString());
            logDto.setResponseBody(bufferingResponse.getBodyAsString());
            logDto.setSuccess(bufferingResponse.getStatusCode().is2xxSuccessful());
            logDto.setErrorMessage(null);

            return bufferingResponse;

        } catch (Exception e) {
            caughtException = e;

            LocalDateTime endTime = LocalDateTime.now();
            long durationMs = Duration.between(startTime, endTime).toMillis();

            logDto.setEndTime(endTime);
            logDto.setDurationMs(durationMs);
            logDto.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            logDto.setResponseHeaders(null);
            logDto.setResponseBody(null);
            logDto.setSuccess(false);
            logDto.setErrorMessage(e.getMessage());

            return null;
        } finally {
            requestLogService.logRequestAsync(logDto);
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

    private static class BufferingClientHttpResponse implements ClientHttpResponse {
        private final ClientHttpResponse response;
        private byte[] body;
        private AtomicBoolean bodyCached = new AtomicBoolean(false);

        public BufferingClientHttpResponse(ClientHttpResponse response) {
            this.response = response;
        }

        @Override
        public HttpStatus getStatusCode() throws IOException {
            return HttpStatus.valueOf(response.getStatusCode().value());
        }

        @Override
        public String getStatusText() throws IOException {
            return response.getStatusText();
        }

        @Override
        public void close() {
            response.close();
        }

        @Override
        public InputStream getBody() throws IOException {
            ensureBodyCached();
            return new ByteArrayInputStream(body);
        }

        @Override
        public org.springframework.http.HttpHeaders getHeaders() {
            return response.getHeaders();
        }

        public String getBodyAsString() {
            try {
                ensureBodyCached();
                return body.length > 0 ? new String(body, StandardCharsets.UTF_8) : "";
            } catch (IOException e) {
                return null;
            }
        }

        private void ensureBodyCached() throws IOException {
            if (bodyCached.get()){
                return;
            }
            if (bodyCached.compareAndSet(false, true)) {
                body = StreamUtils.copyToByteArray(response.getBody());
            }
        }
    }
}