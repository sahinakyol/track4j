package io.track4j.entity;

public enum RequestType {
    INCOMING("INCOMING"),
    EXTERNAL("EXTERNAL"),
    INTERNAL("INTERNAL");

    private final String value;

    RequestType(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}