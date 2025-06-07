package io.track4j.repository;

import io.track4j.objects.RequestLog;

import java.util.List;

public interface RequestLogRepositoryAdapter {
    void save(RequestLog requestLog);
    void saveAll(List<RequestLog> requestLogs);
    void initialize();
    void close();
}