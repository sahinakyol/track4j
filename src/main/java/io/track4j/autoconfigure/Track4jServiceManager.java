package io.track4j.autoconfigure;

import io.track4j.trackers.RestTemplateTrackingInterceptor;
import io.track4j.properties.Track4jProperties;
import io.track4j.repository.RequestLogRepositoryAdapter;
import io.track4j.repository.RequestLogRepositoryFactory;
import io.track4j.service.RequestLogService;
import io.track4j.helper.SerializationService;

public final class Track4jServiceManager {

    private static Track4jServiceManager instance;

    private Track4jProperties properties;
    private RequestLogService requestLogService;
    private RestTemplateTrackingInterceptor restTemplateTrackingInterceptor;
    private SerializationService serializationService;

    private Track4jServiceManager() {
    }

    public static synchronized void initialize() {
        if (instance == null) {
            instance = new Track4jServiceManager();
            instance.startup();
        }
    }

    public static Track4jServiceManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Track4j not initialized");
        }
        return instance;
    }

    public static void shutdown() {
        if (instance != null) {
            instance.stop();
            instance = null;
        }
    }

    private void startup() {
        properties = new Track4jProperties();

        if (!properties.isEnabled()) {
            return;
        }

        RequestLogRepositoryAdapter requestLogRepositoryAdapter = new RequestLogRepositoryFactory(properties).getAdapter();
        serializationService = new SerializationService();

        requestLogService = new RequestLogService(requestLogRepositoryAdapter, properties);
        restTemplateTrackingInterceptor = new RestTemplateTrackingInterceptor(requestLogService);
    }

    private void stop() {
        if (requestLogService != null) {
            requestLogService.shutdown();
        }
    }

    public Track4jProperties getProperties() {
        return properties;
    }

    public RequestLogService getRequestLogService() {
        return requestLogService;
    }

    public RestTemplateTrackingInterceptor getRestTemplateTrackingInterceptor() {
        return restTemplateTrackingInterceptor;
    }

    public SerializationService getSerializationService() {
        return serializationService;
    }
}