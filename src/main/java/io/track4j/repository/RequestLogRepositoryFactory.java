package io.track4j.repository;

import io.track4j.repository.datasourcemanager.Track4jDataSourceManager;
import io.track4j.properties.StorageType;
import io.track4j.properties.Track4jProperties;
import io.track4j.repository.dbrepository.SqlRequestLogRepository;

import javax.sql.DataSource;

public class RequestLogRepositoryFactory {

    private RequestLogRepositoryAdapter adapter;

    public RequestLogRepositoryFactory(Track4jProperties track4jProperties) {
        DataSource track4jDataSource = new Track4jDataSourceManager().getDataSource();
        StorageType storageType = track4jProperties.getStorageType();

        switch (storageType) {
            case SQL:
                if (track4jDataSource == null) {
                    throw new IllegalStateException("DataSource is required for SQL storage");
                }
                setAdapter(new SqlRequestLogRepository(track4jDataSource));
                break;
            case CUSTOM:
                String customClass = track4jProperties.getCustomRepositoryClass();
                if (customClass == null || customClass.isEmpty()) {
                    throw new IllegalStateException("Custom repository class must be specified");
                }
                try {
                    Class<?> clazz = Class.forName(customClass);
                    setAdapter((RequestLogRepositoryAdapter) clazz.getDeclaredConstructor().newInstance());
                    break;
                } catch (Exception e) {
                    throw new IllegalStateException("Failed to instantiate custom repository: " + customClass, e);
                }
            default:
                throw new IllegalArgumentException("Unsupported storage type: " + storageType);
        }
    }

    public RequestLogRepositoryAdapter getAdapter() {
        return this.adapter;
    }

    private void setAdapter(RequestLogRepositoryAdapter adapter) {
        this.adapter = adapter;
    }
}