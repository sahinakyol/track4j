package io.track4j.service;

import io.track4j.dto.RequestLogDto;
import io.track4j.entity.RequestLog;
import io.track4j.properties.Track4jProperties;
import io.track4j.repository.RequestLogRepositoryAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RequestLogService {

    private static final Logger logger = LoggerFactory.getLogger(RequestLogService.class);

    private final Track4jProperties track4jProperties;
    private final RequestLogRepositoryAdapter repository;
    private final BlockingQueue<RequestLogDto> logBuffer;
    private final BlockingQueue<RequestLogDto> rescueBuffer;
    private final ScheduledExecutorService scheduler;
    private final ExecutorService worker;

    public RequestLogService(RequestLogRepositoryAdapter adapter, Track4jProperties track4jProperties) {
        this.track4jProperties = track4jProperties;
        this.repository = adapter;
        this.logBuffer = new ArrayBlockingQueue<>(track4jProperties.getBatchSize());
        this.rescueBuffer = new ArrayBlockingQueue<>(track4jProperties.getBatchSize());
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.worker = Executors.newFixedThreadPool(2);

        if (track4jProperties.isEnabled()) {
            repository.initialize();

            scheduler.scheduleWithFixedDelay(
                    this::processBuffer,
                    0,
                    track4jProperties.getFlushInterval(),
                    TimeUnit.MILLISECONDS
            );

        }
    }

    public void shutdown() {
        worker.shutdown();
        scheduler.shutdown();
        try {
            worker.awaitTermination(30, TimeUnit.SECONDS);
            scheduler.awaitTermination(30, TimeUnit.SECONDS);
            flushRemainingLogs();
        } catch (InterruptedException e) {
            worker.shutdown();
            scheduler.shutdown();
            Thread.currentThread().interrupt();
        } finally {
            repository.close();
        }
    }

    public void logRequestAsync(RequestLogDto dto) {
        try {
            if (!logBuffer.offer(dto, 5, TimeUnit.MILLISECONDS)) {
                RequestLogDto oldLog = logBuffer.poll();
                if (oldLog != null) {
                    rescueBuffer.put(oldLog);
                    logger.warn("Track4j: Request log buffer is full, rescued log");
                }
                logBuffer.put(dto);
            }

            if (logBuffer.size() >= track4jProperties.getBatchSize()) {
                worker.submit(this::processBuffer);
            }

            if (!rescueBuffer.isEmpty()) {
                worker.submit(this::rescueProcessBuffer);
            }

        } catch (InterruptedException e) {
            logger.error("Track4j: Failed to add log to buffer with error : {}", e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    private void processBuffer() {
        List<RequestLogDto> batch = new ArrayList<>(logBuffer.size());
        logBuffer.drainTo(batch, track4jProperties.getBatchSize());

        if (!batch.isEmpty()) {
            processBatch(batch);
        }
    }

    private void rescueProcessBuffer() {
        if (rescueBuffer.isEmpty()) {
            return;
        }

        List<RequestLogDto> batch = new ArrayList<>(rescueBuffer.size());
        rescueBuffer.drainTo(batch, track4jProperties.getBatchSize());

        if (!batch.isEmpty()) {
            processBatch(batch);
        }
    }

    private void processBatch(List<RequestLogDto> batch) {
        try {
            ArrayList<RequestLog> logs = new ArrayList<>(batch.size());
            for (RequestLogDto requestLogDto : batch) {
                logs.add(convertToEntity(requestLogDto));
            }

            repository.saveAll(logs);
        } catch (Exception e) {
            logger.error("Track4j: Failed to save request logs", e);
        }
    }

    private void flushRemainingLogs() {
        while (!logBuffer.isEmpty()) {
            processBuffer();
        }
        while (!rescueBuffer.isEmpty()) {
            rescueProcessBuffer();
        }
    }

    private RequestLog convertToEntity(RequestLogDto dto) {
        RequestLog log = new RequestLog();
        log.setTraceId(dto.getTraceId());
        log.setSpanId(dto.getSpanId());
        log.setParentSpanId(dto.getParentSpanId());
        log.setOperationName(dto.getOperationName());
        log.setRequestType(dto.getRequestType());
        log.setMethod(dto.getMethod());
        log.setUrl(dto.getUrl());
        log.setRequestHeaders(dto.getRequestHeaders());
        log.setRequestBody(dto.getRequestBody());
        log.setResponseHeaders(dto.getResponseHeaders());
        log.setResponseBody(dto.getResponseBody());
        log.setStatusCode(dto.getStatusCode());
        log.setStartTime(dto.getStartTime());
        log.setEndTime(dto.getEndTime());
        log.setDurationMs(dto.getDurationMs());
        log.setSuccess(dto.getSuccess());
        log.setErrorMessage(dto.getErrorMessage());
        log.setUserId(dto.getUserId());
        log.setClientIp(dto.getClientIp());
        log.setTags(dto.getTags());
        return log;
    }
}