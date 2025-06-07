package io.track4j.objects;

public class TrackingResult {
    public final Object result;
    public final Throwable exception;

    public TrackingResult(Object result, Throwable exception) {
        this.result = result;
        this.exception = exception;
    }
}