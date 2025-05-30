package io.track4j.filter;

import io.track4j.autoconfigure.Track4jServiceManager;
import io.track4j.context.TraceContext;
import io.track4j.dto.RequestLogDto;
import io.track4j.entity.RequestLog;
import io.track4j.helper.HttpStatusCode;
import io.track4j.properties.Track4jProperties;
import io.track4j.service.RequestLogService;
import io.track4j.service.SerializationService;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;

public class IncomingRequestTrackingFilter implements Filter {

    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final Track4jProperties track4jProperties = Track4jServiceManager.getInstance().getProperties();
    private RequestLogService requestLogService;
    private final SerializationService serializationService = Track4jServiceManager.getInstance().getSerializationService();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        if (shouldExclude(httpRequest.getRequestURI())) {
            chain.doFilter(request, response);
            return;
        }

        String traceId = httpRequest.getHeader(TraceContext.getTraceIdHeader());
        if (traceId == null || traceId.isBlank()) {
            traceId = TraceContext.generateTraceId();
        }

        String spanId = TraceContext.generateSpanId();
        String parentSpanId = httpRequest.getHeader(TraceContext.getSpanIdHeader());
        if (parentSpanId == null){
            parentSpanId = TraceContext.getSpanId();
        }

        TraceContext.setTraceId(traceId);
        TraceContext.setSpanId(spanId);

        httpResponse.setHeader(TraceContext.getTraceIdHeader(), traceId);
        httpResponse.setHeader(TraceContext.getSpanIdHeader(), spanId);

        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(httpRequest);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(httpResponse);

        LocalDateTime startTime = LocalDateTime.now();
        boolean success = true;
        String errorMessage = null;

        try {
            chain.doFilter(requestWrapper, responseWrapper);
        } catch (Exception e) {
            success = false;
            errorMessage = e.getMessage();
        } finally {
            LocalDateTime endTime = LocalDateTime.now();

            RequestLogDto.Builder builder = RequestLogDto.builder()
                .traceId(traceId)
                .spanId(spanId)
                .parentSpanId(parentSpanId)
                .operationName(requestWrapper.getMethod() + " " + requestWrapper.getRequestURI())
                .requestType(RequestLog.RequestType.INCOMING)
                .method(requestWrapper.getMethod())
                .url(requestWrapper.getRequestURL().toString())
                .statusCode(responseWrapper.getStatus())
                .startTime(startTime)
                .endTime(endTime)
                .durationMs(Duration.between(startTime, endTime).toMillis())
                .success(success && responseWrapper.getStatus() < HttpStatusCode.HTTP_SERVER_INTERNAL_ERROR.getValue())
                .errorMessage(errorMessage)
                .userId(serializationService.extractUserId(requestWrapper))
                .clientIp(serializationService.getClientIp(requestWrapper));

            if (track4jProperties == null || track4jProperties.isIncludeHeaders()) {
                builder.requestHeaders(serializationService.getHeadersAsJson(requestWrapper))
                       .responseHeaders(serializationService.getHeadersAsJson(responseWrapper));
            }

            if (track4jProperties == null || track4jProperties.isIncludeRequestBody()) {
                builder.requestBody(serializationService.getRequestBody(requestWrapper));
            }

            if (track4jProperties == null || track4jProperties.isIncludeResponseBody()) {
                builder.responseBody(serializationService.getResponseBody(responseWrapper));
            }

            getRequestLogService().logRequestAsync(builder.build());

            responseWrapper.copyBodyToResponse();
            TraceContext.clear();
        }
    }

    private boolean shouldExclude(String path) {
        if (track4jProperties == null || track4jProperties.getExcludePatterns() == null) {
            return false;
        }
        
        return Arrays.stream(track4jProperties.getExcludePatterns())
            .anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    private RequestLogService getRequestLogService() {
        if (this.requestLogService == null) {
            this.requestLogService = Track4jServiceManager.getInstance().getRequestLogService();
        }
        return this.requestLogService;
    }
}