package io.track4j.repository;

import io.track4j.autoconfigure.Track4jServiceManager;
import io.track4j.data.Track4jDataSourceManager;
import io.track4j.properties.Track4jProperties;
import io.track4j.repository.dbrepository.SqlRequestLogRepository;

import javax.sql.DataSource;

public class RequestLogRepositoryFactory {

    private RequestLogRepositoryAdapter adapter;
    private final Track4jProperties track4jProperties = Track4jServiceManager.getInstance().getProperties();

    public RequestLogRepositoryFactory() {
        init();
    }

    public void init() {
        DataSource track4jDataSource = new Track4jDataSourceManager().getDataSource();
        Track4jProperties.StorageType storageType = track4jProperties.getStorageType();

        switch (storageType) {
            case SQL:
                if (track4jDataSource == null) {
                    throw new IllegalStateException("DataSource is required for SQL storage");
                }
                adapter = new SqlRequestLogRepository(track4jDataSource);
                break;
            case CUSTOM:
                String customClass = track4jProperties.getCustomRepositoryClass();
                if (customClass == null || customClass.isEmpty()) {
                    throw new IllegalStateException("Custom repository class must be specified");
                }
                try {
                    Class<?> clazz = Class.forName(customClass);
                    adapter = (RequestLogRepositoryAdapter) clazz.getDeclaredConstructor().newInstance();
                    break;
                } catch (Exception e) {
                    throw new IllegalStateException("Failed to instantiate custom repository: " + customClass, e);
                }
            default:
                throw new IllegalArgumentException("Unsupported storage type: " + storageType);
        }
    }

    public RequestLogRepositoryAdapter getAdapter() {
        if (adapter == null) {
            synchronized (this) {
                init();
            }
        }
        return this.adapter;
    }
}