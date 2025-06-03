package io.track4j.repository.dbrepository;

import io.track4j.entity.RequestLog;
import io.track4j.repository.RequestLogRepositoryAdapter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class MongoRequestLogRepository implements RequestLogRepositoryAdapter {

    @Override
    public void initialize() {
        // Not implemented yet
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
            return requestLog;
        } catch (Exception e) {
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
        // Not implemented yet
    }
}