package io.track4j.service;

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
    private final BlockingQueue<RequestLog> logBuffer;
    private final BlockingQueue<RequestLog> rescueBuffer;
    private final ScheduledExecutorService scheduler;
    private final ExecutorService worker;

    public RequestLogService(RequestLogRepositoryAdapter adapter, Track4jProperties track4jProperties) {
        this.track4jProperties = track4jProperties;
        this.repository = adapter;
        this.logBuffer = new ArrayBlockingQueue<>(track4jProperties.getBatchSize());
        this.rescueBuffer = new ArrayBlockingQueue<>(track4jProperties.getBatchSize());
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.worker = Executors.newFixedThreadPool(2);

        repository.initialize();
        scheduler.scheduleWithFixedDelay(
                this::processBuffer,
                0,
                track4jProperties.getFlushInterval(),
                TimeUnit.MILLISECONDS
        );
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

    public void logRequestAsync(RequestLog requestLog) {
        try {
            if (!logBuffer.offer(requestLog, 5, TimeUnit.MILLISECONDS)) {
                RequestLog oldLog = logBuffer.poll();
                if (oldLog != null) {
                    rescueBuffer.put(oldLog);
                    logger.warn("Track4j: Request log buffer is full, rescued log");
                }
                logBuffer.put(requestLog);
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
        List<RequestLog> batch = new ArrayList<>(logBuffer.size());
        logBuffer.drainTo(batch, track4jProperties.getBatchSize());

        if (!batch.isEmpty()) {
            processBatch(batch);
        }
    }

    private void rescueProcessBuffer() {
        if (rescueBuffer.isEmpty()) {
            return;
        }

        List<RequestLog> batch = new ArrayList<>(rescueBuffer.size());
        rescueBuffer.drainTo(batch, track4jProperties.getBatchSize());

        if (!batch.isEmpty()) {
            processBatch(batch);
        }
    }

    private void processBatch(List<RequestLog> logs) {
        try {
            repository.saveAll(logs);
        } catch (Exception e) {
            logger.error("Track4j: Failed to save request logs", e);
        } finally {
            logs.clear();
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
}