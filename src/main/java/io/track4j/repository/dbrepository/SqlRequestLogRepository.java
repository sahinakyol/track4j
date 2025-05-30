package io.track4j.repository.dbrepository;

import io.track4j.entity.RequestLog;
import io.track4j.repository.RequestLogRepositoryAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class SqlRequestLogRepository implements RequestLogRepositoryAdapter {

    private static final Logger logger = LoggerFactory.getLogger(SqlRequestLogRepository.class);
    private final DataSource track4jDataSource;

    private static final String INSERT_SQL = "INSERT INTO " +
            "request_logs (id, trace_id, span_id, parent_span_id, operation_name, request_type,method, url," +
            " request_headers, request_body, response_headers,response_body, status_code, start_time, end_time," +
            " duration_ms,success, error_message, user_id, client_ip, tags, created_at) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    public SqlRequestLogRepository(DataSource track4jDataSource) {
        this.track4jDataSource = track4jDataSource;
    }

    @Override
    public void initialize() {
        try (Connection conn = track4jDataSource.getConnection()) {

            String dbProductName = conn.getMetaData().getDatabaseProductName().toLowerCase();

            logger.info("Track4j: Initialized SQL request_logs table {}", dbProductName);
        } catch (SQLException e) {
            logger.error("Track4j: Failed to initialize database", e);
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

        try (Connection conn = track4jDataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_SQL)) {

            setParameters(ps, requestLog);
            ps.executeUpdate();
            return requestLog;

        } catch (SQLException e) {
            logger.error("Track4j: Failed to save request log", e);
            return new RequestLog();
        }
    }

    @Override
    public List<RequestLog> saveAll(List<RequestLog> requestLogs) {
        if (requestLogs.isEmpty()) {
            return requestLogs;
        }

        try (Connection conn = track4jDataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_SQL)) {

            conn.setAutoCommit(false);

            for (RequestLog log : requestLogs) {
                if (log.getId() == null) {
                    log.setId(UUID.randomUUID().toString());
                }
                if (log.getCreatedAt() == null) {
                    log.setCreatedAt(LocalDateTime.now());
                }

                setParameters(ps, log);
                ps.addBatch();
            }

            ps.executeBatch();
            conn.commit();
            return requestLogs;

        } catch (SQLException e) {
            logger.error("Track4j: Failed to save batch of request logs", e);
            return Collections.emptyList();
        }
    }

    @Override
    public void close() {
        try {
            track4jDataSource.getConnection().close();
        } catch (SQLException e){

        }
    }

    private void setParameters(PreparedStatement ps, RequestLog log) throws SQLException {
        ps.setString(1, log.getId());
        ps.setString(2, log.getTraceId());
        ps.setString(3, log.getSpanId());
        ps.setString(4, log.getParentSpanId());
        ps.setString(5, log.getOperationName());
        ps.setString(6, log.getRequestType() != null ? log.getRequestType().name() : null);
        ps.setString(7, log.getMethod());
        ps.setString(8, log.getUrl());
        ps.setString(9, log.getRequestHeaders());
        ps.setString(10, log.getRequestBody());
        ps.setString(11, log.getResponseHeaders());
        ps.setString(12, log.getResponseBody());
        ps.setObject(13, log.getStatusCode());
        ps.setTimestamp(14, log.getStartTime() != null ? Timestamp.valueOf(log.getStartTime()) : null);
        ps.setTimestamp(15, log.getEndTime() != null ? Timestamp.valueOf(log.getEndTime()) : null);
        ps.setObject(16, log.getDurationMs());
        ps.setObject(17, log.getSuccess());
        ps.setString(18, log.getErrorMessage());
        ps.setString(19, log.getUserId());
        ps.setString(20, log.getClientIp());
        ps.setString(21, log.getTags());
        ps.setTimestamp(22, Timestamp.valueOf(log.getCreatedAt()));
    }
}