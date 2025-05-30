package io.track4j.dto;

public class TrackingResult {
    public final Object result;
    public final Throwable exception;

    public TrackingResult(Object result, Throwable exception) {
        this.result = result;
        this.exception = exception;
    }
}