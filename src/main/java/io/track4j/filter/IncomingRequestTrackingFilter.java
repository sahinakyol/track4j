package io.track4j.filter;

import io.track4j.autoconfigure.Track4jServiceManager;
import io.track4j.context.TraceContext;
import io.track4j.dto.LightweightRequestWrapper;
import io.track4j.dto.LightweightResponseWrapper;
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

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;

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

        TraceContext.TraceInfo existingTrace = TraceContext.getTraceInfo();

        String traceId = httpRequest.getHeader(TraceContext.getTraceIdHeader());
        if (traceId == null || traceId.isBlank()) {
            traceId = TraceContext.generateTraceId();
        }

        String spanId = TraceContext.generateSpanId();
        String parentSpanId = httpRequest.getHeader(TraceContext.getSpanIdHeader());
        if (parentSpanId == null) {
            parentSpanId = existingTrace.spanId;
        }

        TraceContext.setTraceData(traceId, spanId);

        httpResponse.setHeader(TraceContext.getTraceIdHeader(), traceId);
        httpResponse.setHeader(TraceContext.getSpanIdHeader(), spanId);

        LightweightRequestWrapper requestWrapper = new LightweightRequestWrapper(httpRequest);
        LightweightResponseWrapper responseWrapper = new LightweightResponseWrapper(httpResponse);

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

            long durationMs = Duration.between(startTime, endTime).toMillis();
            int statusCode = responseWrapper.getStatus();
            boolean isSuccess = success && statusCode < HttpStatusCode.HTTP_SERVER_INTERNAL_ERROR.getValue();

            RequestLogDto dto = new RequestLogDto();

            dto.setTraceId(traceId);
            dto.setSpanId(spanId);
            dto.setParentSpanId(parentSpanId);
            dto.setOperationName(requestWrapper.getMethod() + " " + requestWrapper.getRequestURI());
            dto.setRequestType(RequestLog.RequestType.INCOMING);
            dto.setMethod(requestWrapper.getMethod());
            dto.setUrl(requestWrapper.getRequestURL().toString());
            dto.setStatusCode(statusCode);
            dto.setStartTime(startTime);
            dto.setEndTime(endTime);
            dto.setDurationMs(durationMs);
            dto.setSuccess(isSuccess);
            dto.setErrorMessage(errorMessage);
            dto.setUserId(serializationService.extractUserId(requestWrapper));
            dto.setClientIp(serializationService.getClientIp(requestWrapper));


            if (track4jProperties == null || track4jProperties.isIncludeHeaders()) {
                dto.setRequestHeaders(serializationService.getHeadersAsJson(requestWrapper));
                dto.setResponseHeaders(serializationService.getHeadersAsJson(responseWrapper));
            }

            if (track4jProperties == null || track4jProperties.isIncludeRequestBody()) {
                dto.setRequestBody(serializationService.getRequestBody(requestWrapper));
            }

            if (track4jProperties == null || track4jProperties.isIncludeResponseBody()) {
                dto.setResponseBody(serializationService.getResponseBody(responseWrapper));
            }

            getRequestLogService().logRequestAsync(dto);

            responseWrapper.copyBodyToResponse();
            TraceContext.clear();
        }
    }

    private boolean shouldExclude(String path) {
        if (track4jProperties == null || track4jProperties.getExcludePatterns() == null) {
            return false;
        }

        String[] patterns = track4jProperties.getExcludePatterns();
        for (String pattern : patterns) {
            if (pathMatcher.match(pattern, path)) {
                return true;
            }
        }
        return false;
    }

    private RequestLogService getRequestLogService() {
        if (this.requestLogService == null) {
            this.requestLogService = Track4jServiceManager.getInstance().getRequestLogService();
        }
        return this.requestLogService;
    }
}