package io.track4j.properties;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Track4jProperties {
    private static final Logger logger = Logger.getLogger(Track4jProperties.class.getName());

    private boolean enabled = true;
    private boolean incomingRequestTrackingEnabled = true;
    private boolean externalRequestTrackingEnabled = true;
    private boolean internalCallTrackingEnabled = true;
    private int filterOrder = -100;
    private int batchSize = 100;
    private long flushInterval = 5000;
    private boolean includeRequestBody = true;
    private boolean includeResponseBody = true;
    private boolean includeHeaders = true;
    private int maxBodySize = 10240;
    private String[] excludePatterns = {};

    private StorageType storageType = StorageType.SQL;
    private String customRepositoryClass;
    private String connectionUrl;
    private String connectionUsername;
    private String connectionPassword;
    private String driverClassName;

    private int maximumPoolSize = 5;
    private int minimumIdle = 1;
    private int connectionTimeout = 30000;
    private int idleTimeout = 600000;
    private int maxLifetime = 1800000;
    private boolean autoCommit = false;
    private String poolName = "Track4jPool";

    public Track4jProperties() {
        Properties properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("track4j.properties")) {
            if (input == null) {
                logger.log(Level.WARNING, "Sorry, unable to find application.properties");
                return;
            }
            properties.load(input);

            this.enabled = Boolean.parseBoolean(properties.getProperty("track4j.enabled"));
            this.storageType = StorageType.valueOf(properties.getProperty("track4j.storage-type", storageType.name()).toUpperCase());
            this.connectionUrl = properties.getProperty("track4j.connection-url");
            this.connectionUsername = properties.getProperty("track4j.connection-username");
            this.connectionPassword = properties.getProperty("track4j.connection-password");
            this.driverClassName = properties.getProperty("track4j.driver-class-name");
            this.customRepositoryClass = properties.getProperty("track4j.custom-repository-class");


            this.maximumPoolSize = Integer.parseInt(properties.getProperty("track4j.hikari.maximum-pool-size"));
            this.minimumIdle = Integer.parseInt(properties.getProperty("track4j.hikari.minimum-idle"));
            this.connectionTimeout = Integer.parseInt(properties.getProperty("track4j.hikari.connection-timeout"));
            this.idleTimeout = Integer.parseInt(properties.getProperty("track4j.hikari.idle-timeout"));
            this.maxLifetime = Integer.parseInt(properties.getProperty("track4j.hikari.max-lifetime"));
            this.autoCommit = Boolean.parseBoolean(properties.getProperty("track4j.hikari.auto-commit"));
            this.poolName = properties.getProperty("track4j.hikari.pool-name");


            this.incomingRequestTrackingEnabled = Boolean.parseBoolean(properties.getProperty("track4j.incoming-request-tracking-enabled"));
            this.externalRequestTrackingEnabled = Boolean.parseBoolean(properties.getProperty("track4j.external-request-tracking-enabled"));
            this.internalCallTrackingEnabled = Boolean.parseBoolean(properties.getProperty("track4j.internal-call-tracking-enabled"));
            this.filterOrder = Integer.parseInt(properties.getProperty("track4j.filter-order"));
            this.batchSize = Integer.parseInt(properties.getProperty("track4j.batch-size"));
            this.flushInterval = Integer.parseInt(properties.getProperty("track4j.flush-interval"));
            this.includeRequestBody = Boolean.parseBoolean(properties.getProperty("track4j.include-request-body"));
            this.includeResponseBody = Boolean.parseBoolean(properties.getProperty("track4j.include-response-body"));
            this.includeHeaders = Boolean.parseBoolean(properties.getProperty("track4j.include-headers"));
            this.maxBodySize = Integer.parseInt(properties.getProperty("track4j.max-body-size"));
            String rawPatterns = properties.getProperty("track4j.exclude-patterns");
            if (rawPatterns != null && !rawPatterns.isBlank()) {
                String[] patterns = rawPatterns.split("\\s*,\\s*");
                setExcludePatterns(new String[patterns.length]);
                for (int i = 0; i < patterns.length; i++) {
                    this.excludePatterns[i] = patterns[i];
                }

            }
        } catch (IOException ex) {
            ex.printStackTrace();
            enabled = false;
        }

    }

    public String getConnectionUrl() {
        return this.connectionUrl;
    }

    public void setConnectionUrl(String connectionUrl) {
        this.connectionUrl = connectionUrl;
    }

    public String getConnectionUsername() {
        return this.connectionUsername;
    }

    public void setConnectionUsername(String connectionUsername) {
        this.connectionUsername = connectionUsername;
    }

    public String getConnectionPassword() {
        return this.connectionPassword;
    }

    public void setConnectionPassword(String connectionPassword) {
        this.connectionPassword = connectionPassword;
    }

    public String getDriverClassName() {
        return this.driverClassName;
    }

    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    public int getMaximumPoolSize() {
        return maximumPoolSize;
    }

    public void setMaximumPoolSize(int maximumPoolSize) {
        this.maximumPoolSize = maximumPoolSize;
    }

    public int getMinimumIdle() {
        return minimumIdle;
    }

    public void setMinimumIdle(int minimumIdle) {
        this.minimumIdle = minimumIdle;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public int getIdleTimeout() {
        return idleTimeout;
    }

    public void setIdleTimeout(int idleTimeout) {
        this.idleTimeout = idleTimeout;
    }

    public int getMaxLifetime() {
        return maxLifetime;
    }

    public void setMaxLifetime(int maxLifetime) {
        this.maxLifetime = maxLifetime;
    }

    public boolean isAutoCommit() {
        return autoCommit;
    }

    public void setAutoCommit(boolean autoCommit) {
        this.autoCommit = autoCommit;
    }

    public String getPoolName() {
        return poolName;
    }

    public void setPoolName(String poolName) {
        this.poolName = poolName;
    }

    public enum StorageType {
        SQL,
        CUSTOM
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isIncomingRequestTrackingEnabled() {
        return incomingRequestTrackingEnabled;
    }

    public void setIncomingRequestTrackingEnabled(boolean enabled) {
        this.incomingRequestTrackingEnabled = enabled;
    }

    public boolean isExternalRequestTrackingEnabled() {
        return externalRequestTrackingEnabled;
    }

    public void setExternalRequestTrackingEnabled(boolean enabled) {
        this.externalRequestTrackingEnabled = enabled;
    }

    public boolean isInternalCallTrackingEnabled() {
        return internalCallTrackingEnabled;
    }

    public void setInternalCallTrackingEnabled(boolean enabled) {
        this.internalCallTrackingEnabled = enabled;
    }

    public int getFilterOrder() {
        return filterOrder;
    }

    public void setFilterOrder(int filterOrder) {
        this.filterOrder = filterOrder;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public long getFlushInterval() {
        return flushInterval;
    }

    public void setFlushInterval(long flushInterval) {
        this.flushInterval = flushInterval;
    }

    public boolean isIncludeRequestBody() {
        return includeRequestBody;
    }

    public void setIncludeRequestBody(boolean includeRequestBody) {
        this.includeRequestBody = includeRequestBody;
    }

    public boolean isIncludeResponseBody() {
        return includeResponseBody;
    }

    public void setIncludeResponseBody(boolean includeResponseBody) {
        this.includeResponseBody = includeResponseBody;
    }

    public boolean isIncludeHeaders() {
        return includeHeaders;
    }

    public void setIncludeHeaders(boolean includeHeaders) {
        this.includeHeaders = includeHeaders;
    }

    public int getMaxBodySize() {
        return maxBodySize;
    }

    public void setMaxBodySize(int maxBodySize) {
        this.maxBodySize = maxBodySize;
    }

    public String[] getExcludePatterns() {
        return excludePatterns;
    }

    public void setExcludePatterns(String[] excludePatterns) {
        this.excludePatterns = excludePatterns;
    }

    public StorageType getStorageType() {
        return storageType;
    }

    public void setStorageType(StorageType storageType) {
        this.storageType = storageType;
    }

    public String getCustomRepositoryClass() {
        return customRepositoryClass;
    }

    public void setCustomRepositoryClass(String customRepositoryClass) {
        this.customRepositoryClass = customRepositoryClass;
    }
}