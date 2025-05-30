package io.track4j.interceptor;

import io.track4j.context.TraceContext;
import io.track4j.dto.RequestLogDto;
import io.track4j.service.RequestLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

public class RestTemplateTrackingInterceptor implements ClientHttpRequestInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(RestTemplateTrackingInterceptor.class);

    private final RequestLogService requestLogService;

    public RestTemplateTrackingInterceptor(RequestLogService requestLogService) {
        this.requestLogService = requestLogService;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                        ClientHttpRequestExecution execution) throws IOException {

        String traceId = TraceContext.getTraceId();
        if (traceId == null || traceId.isBlank()) {
            traceId = TraceContext.generateTraceId();
        }

        String parentSpanId = TraceContext.getSpanId();
        String spanId = TraceContext.generateSpanId();

        request.getHeaders().set(TraceContext.getTraceIdHeader(), traceId);
        request.getHeaders().set(TraceContext.getSpanIdHeader(), spanId);

        RequestLogDto logDto = RequestLogDto.fromExternalRequest(
                request.getURI().toString(),
                request.getMethod().toString(),
                traceId,
                spanId
        );
        logDto.setParentSpanId(parentSpanId);
        logDto.setRequestHeaders(request.getHeaders().toString());
        logDto.setRequestBody(new String(body, StandardCharsets.UTF_8));

        boolean success = true;
        String errorMessage = null;
        BufferingClientHttpResponse response = null;

        try {
            response = new BufferingClientHttpResponse(execution.execute(request, body));
            return response;
        } catch (Exception e) {
            success = false;
            errorMessage = e.getMessage();
            logger.error("Track4j: InternalServiceTrackingAspect.executeWithTracking, trackingResult failed to get", errorMessage);
            return null;
        } finally {
            if (response != null) {
                logDto.completeWithResponse(
                        response.getStatusCode().value(),
                        response.getHeaders().toString(),
                        response.getBodyAsString(),
                        success && response.getStatusCode().is2xxSuccessful(),
                        errorMessage
                );
            } else {
                logDto.completeWithResponse(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        null,
                        null,
                        false,
                        errorMessage
                );
            }

            requestLogService.logRequestAsync(logDto);
        }
    }

    private static class BufferingClientHttpResponse implements ClientHttpResponse {
        private final ClientHttpResponse response;
        private byte[] body;

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
            if (body == null) {
                body = StreamUtils.copyToByteArray(response.getBody());
            }
            return new ByteArrayInputStream(body);
        }

        @Override
        public org.springframework.http.HttpHeaders getHeaders() {
            return response.getHeaders();
        }

        public String getBodyAsString() {
            try {
                getBody();
                return new String(body, StandardCharsets.UTF_8);
            } catch (IOException e) {
                return null;
            }
        }
    }
}