package io.track4j.aspect;

import io.track4j.annotation.Track4j;
import io.track4j.autoconfigure.Track4jServiceManager;
import io.track4j.context.TraceContext;
import io.track4j.dto.TrackingResult;
import io.track4j.entity.RequestLog;
import io.track4j.entity.RequestType;
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
import java.time.Duration;
import java.time.LocalDateTime;

@Aspect
public class InternalServiceTrackingAspect {
    private static final Track4jProperties track4jProperties = Track4jServiceManager.getInstance().getProperties();
    private static final RequestLogService requestLogService = Track4jServiceManager.getInstance().getRequestLogService();
    private static final SerializationService serializationService = Track4jServiceManager.getInstance().getSerializationService();

    @Around("@annotation(io.track4j.annotation.Track4j) || @within(io.track4j.annotation.Track4j)")
    public Object trackAnnotatedCalls(ProceedingJoinPoint pjp) throws Throwable {

        Track4j trackableConfig = getTrackableAnnotation(pjp);
        if (trackableConfig == null || !trackableConfig.enabled()
                || !track4jProperties.isEnabled() || !track4jProperties.isInternalCallTrackingEnabled()) {
            return pjp.proceed();
        }

        TraceContext.TraceInfo currentTrace = TraceContext.getTraceInfo();

        String traceId = currentTrace.traceId;
        if (traceId == null || traceId.isBlank()) {
            traceId = TraceContext.generateTraceId();
        }

        return executeWithTracking(pjp, trackableConfig, traceId, currentTrace.spanId);
    }

    private Object executeWithTracking(ProceedingJoinPoint pjp,
                                       Track4j config,
                                       String traceId,
                                       String parentSpanId) {

        String spanId = TraceContext.generateSpanId();
        String className = getClassName(pjp);
        String methodName = getMethodName(pjp);
        String operationName = buildOperationName(config, className, methodName);

        LocalDateTime startTime = LocalDateTime.now();

        RequestLog requestLog = new RequestLog();
        requestLog.setTraceId(traceId);
        requestLog.setSpanId(spanId);
        requestLog.setParentSpanId(parentSpanId);
        requestLog.setOperationName(operationName);
        requestLog.setRequestType(RequestType.INTERNAL);
        requestLog.setMethod(RequestType.INTERNAL.getValue());
        requestLog.setUrl(className + "." + methodName);
        requestLog.setStartTime(startTime);
        requestLog.setTags(config.tags().length > 0 ? String.join(",", config.tags()) : null);

        if (config.includeArgs()) {
            requestLog.setRequestBody(serializationService.serializeArgs(pjp.getArgs()));
        }

        TrackingResult trackingResult = executeWithSpanContext(pjp, spanId, traceId);

        LocalDateTime endTime = LocalDateTime.now();
        long durationMs = Duration.between(startTime, endTime).toMillis();

        if (config.includeResult() && trackingResult.result != null) {
            requestLog.setResponseBody(serializationService.serializeResult(trackingResult.result));
        }

        requestLog.setEndTime(endTime);
        requestLog.setDurationMs(durationMs);
        requestLog.setStatusCode(HttpStatusCode.HTTP_SUCCESS.getValue());
        requestLog.setSuccess(trackingResult.exception == null);
        requestLog.setErrorMessage(trackingResult.exception != null ? trackingResult.exception.getMessage() : null);

        requestLogService.logRequestAsync(requestLog);

        if (trackingResult.exception != null) {
            return new Object();
        }

        return trackingResult.result;
    }

    private TrackingResult executeWithSpanContext(ProceedingJoinPoint pjp, String spanId, String traceId) {
        TraceContext.TraceInfo originalContext = TraceContext.getTraceInfo();
        Object result = null;
        Throwable exception = null;

        try {
            TraceContext.setTraceData(traceId, spanId);
            result = pjp.proceed();
        } catch (Throwable e) {
            exception = e;
        } finally {
            TraceContext.setTraceData(originalContext.traceId, originalContext.spanId);
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

    private String buildOperationName(Track4j config,
                                      String className, String methodName) {
        if (StringUtils.hasText(config.name())) {
            return config.name();
        }
        return className + "." + methodName;
    }

    private String getClassName(ProceedingJoinPoint pjp) {
        return pjp.getTarget().getClass().getSimpleName();
    }

    private String getMethodName(ProceedingJoinPoint pjp) {
        return pjp.getSignature().getName();
    }

}