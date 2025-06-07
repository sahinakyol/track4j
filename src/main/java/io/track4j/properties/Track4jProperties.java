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
    private String[] excludePatterns = {};

    private StorageType storageType = StorageType.SQL;
    private String customRepositoryClass = "";
    private String connectionUrl = "";
    private String connectionUsername = "";
    private String connectionPassword = "";
    private String driverClassName = "";

    private int maximumPoolSize = 5;
    private int minimumIdle = 1;
    private long connectionTimeout = 30000;
    private long idleTimeout = 600000;
    private long maxLifetime = 1800000;
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
            this.connectionTimeout = Long.parseLong(properties.getProperty("track4j.hikari.connection-timeout"));
            this.idleTimeout = Long.parseLong(properties.getProperty("track4j.hikari.idle-timeout"));
            this.maxLifetime = Long.parseLong(properties.getProperty("track4j.hikari.max-lifetime"));
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
            String rawPatterns = properties.getProperty("track4j.exclude-patterns");
            if (rawPatterns != null && !rawPatterns.isBlank()) {
                String[] patterns = rawPatterns.split("\\s*,\\s*");
                setExcludePatterns(new String[patterns.length]);
                System.arraycopy(patterns, 0, this.excludePatterns, 0, patterns.length);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            enabled = false;
        }

    }

    public String getConnectionUrl() {
        return this.connectionUrl;
    }

    public String getConnectionUsername() {
        return this.connectionUsername;
    }

    public String getConnectionPassword() {
        return this.connectionPassword;
    }

    public String getDriverClassName() {
        return this.driverClassName;
    }

    public int getMaximumPoolSize() {
        return maximumPoolSize;
    }

    public int getMinimumIdle() {
        return minimumIdle;
    }

    public long getConnectionTimeout() {
        return connectionTimeout;
    }

    public long getIdleTimeout() {
        return idleTimeout;
    }

    public long getMaxLifetime() {
        return maxLifetime;
    }

    public boolean isAutoCommit() {
        return autoCommit;
    }

    public String getPoolName() {
        return poolName;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isIncomingRequestTrackingEnabled() {
        return incomingRequestTrackingEnabled;
    }

    public boolean isExternalRequestTrackingEnabled() {
        return externalRequestTrackingEnabled;
    }

    public boolean isInternalCallTrackingEnabled() {
        return internalCallTrackingEnabled;
    }

    public int getFilterOrder() {
        return filterOrder;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public long getFlushInterval() {
        return flushInterval;
    }

    public boolean isIncludeRequestBody() {
        return includeRequestBody;
    }

    public boolean isIncludeResponseBody() {
        return includeResponseBody;
    }

    public boolean isIncludeHeaders() {
        return includeHeaders;
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

    public String getCustomRepositoryClass() {
        return customRepositoryClass;
    }
}