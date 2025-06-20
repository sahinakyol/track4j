package io.track4j.helper;

public enum HttpStatusCode {
    HTTP_SUCCESS(200),
    HTTP_SERVER_INTERNAL_ERROR(500),
    HTTP_REDIRECT(300);

    private final int value;

    HttpStatusCode(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }
}
