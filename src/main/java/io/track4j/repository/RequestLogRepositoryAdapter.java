package io.track4j.repository;

import io.track4j.entity.RequestLog;

import java.util.List;

public interface RequestLogRepositoryAdapter {
    RequestLog save(RequestLog requestLog);
    List<RequestLog> saveAll(List<RequestLog> requestLogs);
    void initialize();
    void close();
}