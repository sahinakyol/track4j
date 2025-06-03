package io.track4j.service;

import java.util.ArrayList;
import java.util.Iterator;

public class HttpHeaders implements Iterable<HttpHeader>{
    private final ArrayList<HttpHeader> headers;

    public HttpHeaders() {
      this.headers = new ArrayList<>();
    }

    public void addHeader(HttpHeader httpHeader) {
        this.headers.add(httpHeader);
    }

    public boolean contains(HttpHeader httpHeader) {
        return this.headers.contains(httpHeader);
    }

    @Override
    public Iterator<HttpHeader> iterator() {
        return this.headers.iterator();
    }
}

