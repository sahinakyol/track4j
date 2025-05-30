package io.track4j.data;

import com.zaxxer.hikari.HikariDataSource;
import io.track4j.autoconfigure.Track4jServiceManager;
import io.track4j.properties.Track4jProperties;
import jakarta.annotation.PreDestroy;

import javax.sql.DataSource;

public class Track4jDataSourceManager {
    private final HikariDataSource dataSource;
    
    public Track4jDataSourceManager() {
        this.dataSource = new HikariDataSource();
        Track4jProperties track4jProperties = Track4jServiceManager.getInstance().getProperties();
        dataSource.setJdbcUrl(track4jProperties.getConnectionUrl());
        if (track4jProperties.getConnectionUsername() != null && track4jProperties.getConnectionPassword() != null) {
            dataSource.setUsername(track4jProperties.getConnectionUsername());
            dataSource.setPassword(track4jProperties.getConnectionPassword());
        }

        dataSource.setMaximumPoolSize(track4jProperties.getMaximumPoolSize());
        dataSource.setMinimumIdle(track4jProperties.getMinimumIdle());
        dataSource.setConnectionTimeout(track4jProperties.getConnectionTimeout());
        dataSource.setIdleTimeout(track4jProperties.getIdleTimeout());
        dataSource.setMaxLifetime(track4jProperties.getMaxLifetime());
        dataSource.setAutoCommit(track4jProperties.isAutoCommit());
        dataSource.setPoolName(track4jProperties.getPoolName());
        dataSource.setDriverClassName(track4jProperties.getDriverClassName());
    }
    
    public DataSource getDataSource() {
        return dataSource;
    }
    
    @PreDestroy
    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}