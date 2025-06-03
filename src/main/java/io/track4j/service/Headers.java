package io.track4j.service;

import java.util.ArrayList;
import java.util.Iterator;

public class Headers implements Iterable<HttpHeader>{
    private final ArrayList<HttpHeader> httpHeaders;

    public Headers() {
      this.httpHeaders = new ArrayList<>();
    }

    public void addHeader(HttpHeader httpHeader) {
        this.httpHeaders.add(httpHeader);
    }

    public boolean contains(HttpHeader httpHeader) {
        return this.httpHeaders.contains(httpHeader);
    }

    @Override
    public Iterator<HttpHeader> iterator() {
        return this.httpHeaders.iterator();
    }
}

