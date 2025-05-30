package io.track4j.repository.dbrepository;

import io.track4j.entity.RequestLog;
import io.track4j.repository.RequestLogRepositoryAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class MongoRequestLogRepository implements RequestLogRepositoryAdapter {

    private static final Logger logger = LoggerFactory.getLogger(MongoRequestLogRepository.class);

    @Override
    public void initialize() {
        try {
        } catch (Exception e) {
        }
    }
    
    @Override
    public RequestLog save(RequestLog requestLog) {
        if (requestLog.getId() == null) {
            requestLog.setId(UUID.randomUUID().toString());
        }
        if (requestLog.getCreatedAt() == null) {
            requestLog.setCreatedAt(LocalDateTime.now());
        }
        
        try {
            String json = toJson(requestLog);
            //create
            
            return requestLog;
        } catch (Exception e) {
            logger.error("Track4j: Failed to save request log", e);
            return new RequestLog();
        }
    }
    
    @Override
    public List<RequestLog> saveAll(List<RequestLog> requestLogs) {
        return requestLogs.stream()
                .map(this::save)
                .collect(Collectors.toList());
    }

    @Override
    public void close() {
    }

    private String toJson(RequestLog log) {
        return String.format(
            "{\"id\":\"%s\",\"traceId\":\"%s\",\"spanId\":\"%s\",\"operationName\":\"%s\"," +
            "\"method\":\"%s\",\"url\":\"%s\",\"statusCode\":%d,\"durationMs\":%d," +
            "\"success\":%s,\"createdAt\":\"%s\"}",
            log.getId(), log.getTraceId(), log.getSpanId(), log.getOperationName(),
            log.getMethod(), log.getUrl(), log.getStatusCode(), log.getDurationMs(),
            log.getSuccess(), log.getCreatedAt()
        );
    }
}