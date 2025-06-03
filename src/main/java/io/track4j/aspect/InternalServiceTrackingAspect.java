package io.track4j.aspect;

import io.track4j.annotation.Track4j;
import io.track4j.autoconfigure.Track4jServiceManager;
import io.track4j.context.TraceContext;
import io.track4j.dto.RequestLogDto;
import io.track4j.dto.TrackingResult;
import io.track4j.helper.HttpStatusCode;
import io.track4j.properties.Track4jProperties;
import io.track4j.service.RequestLogService;
import io.track4j.service.SerializationService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;

@Aspect
public class InternalServiceTrackingAspect {
    private final Track4jProperties track4jProperties = Track4jServiceManager.getInstance().getProperties();
    private final RequestLogService requestLogService = Track4jServiceManager.getInstance().getRequestLogService();
    private final SerializationService serializationService = Track4jServiceManager.getInstance().getSerializationService();

    @Around("@annotation(io.track4j.annotation.Track4j) || @within(io.track4j.annotation.Track4j)")
    public Object trackAnnotatedCalls(ProceedingJoinPoint pjp) throws Throwable {

        Track4j trackableConfig = getTrackableAnnotation(pjp);
        if (trackableConfig == null || !trackableConfig.enabled() || !track4jProperties.isEnabled() || !track4jProperties.isInternalCallTrackingEnabled()) {
            return pjp.proceed();
        }

        String traceId = TraceContext.getTraceId();
        if (traceId == null || traceId.isBlank()) {
            traceId = TraceContext.generateTraceId();
        }

        return executeWithTracking(pjp, trackableConfig, traceId);
    }

    private Object executeWithTracking(ProceedingJoinPoint pjp,
                                       Track4j config,
                                       String traceId) {
        String parentSpanId = TraceContext.getSpanId();
        String spanId = TraceContext.generateSpanId();
        String operationName = buildOperationName(pjp, config);

        RequestLogDto logDto = RequestLogDto.fromInternalCall(
                getClassName(pjp),
                getMethodName(pjp),
                traceId,
                spanId
        );

        logDto.setParentSpanId(parentSpanId);
        logDto.setOperationName(operationName);
        logDto.setTags(String.join(",", config.tags()));

        if (config.includeArgs()) {
            logDto.setRequestBody(serializationService.serializeArgs(pjp.getArgs()));
        }

        TrackingResult trackingResult = executeWithSpanContext(pjp, spanId, traceId);

        if (config.includeResult() && trackingResult.result != null) {
            logDto.setResponseBody(serializationService.serializeResult(trackingResult.result));
        }

        completeLog(logDto, trackingResult);

        if (trackingResult.exception != null) {
            return new Object();
        }

        return trackingResult.result;
    }

    private TrackingResult executeWithSpanContext(ProceedingJoinPoint pjp, String spanId, String traceId) {
        String originalSpanId = TraceContext.getSpanId();
        Object result = null;
        Throwable exception = null;

        try {
            TraceContext.setSpanId(spanId);
            TraceContext.setTraceId(traceId);
            result = pjp.proceed();
        } catch (Throwable e) {
            exception = e;
        } finally {
            TraceContext.setSpanId(originalSpanId);
        }

        return new TrackingResult(result, exception);
    }

    private Track4j getTrackableAnnotation(ProceedingJoinPoint pjp) {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();

        Track4j methodAnnotation = AnnotationUtils.findAnnotation(method, Track4j.class);
        if (methodAnnotation != null) {
            return methodAnnotation;
        }

        return AnnotationUtils.findAnnotation(pjp.getTarget().getClass(), Track4j.class);
    }

    private String buildOperationName(ProceedingJoinPoint pjp, Track4j config) {
        if (StringUtils.hasText(config.name())) {
            return config.name();
        }

        return getClassName(pjp) + "." + getMethodName(pjp);
    }

    private void completeLog(RequestLogDto logDto, TrackingResult result) {
        logDto.completeWithResponse(
                HttpStatusCode.HTTP_SUCCESS.getValue(),
                logDto.getResponseHeaders(),
                logDto.getResponseBody(),
                result.exception == null,
                result.exception != null ? result.exception.getMessage() : null
        );

        requestLogService.logRequestAsync(logDto);
    }

    private String getClassName(ProceedingJoinPoint pjp) {
        return pjp.getTarget().getClass().getSimpleName();
    }

    private String getMethodName(ProceedingJoinPoint pjp) {
        return pjp.getSignature().getName();
    }

}