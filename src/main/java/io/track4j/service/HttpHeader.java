package io.track4j.service;

public class HttpHeader {
    private final String headerName;

    public HttpHeader(String headerName) {
        this.headerName = headerName;
    }

    public String getHeaderName() {
        return headerName;
    }
}
