package io.track4j.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.track4j.dto.LightweightRequestWrapper;
import io.track4j.dto.LightweightResponseWrapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.Iterator;

public final class SerializationService {

    private final ObjectMapper track4jObjectMapper;
    private final HttpHeaders sensitiveHttpHeaders;
    private final HttpHeaders ipHttpHeaders;
    private static final String STRING_EMPTY_ARRAY = "[]";
    private static final String STRING_ERROR_ARRAY = "[Error: %s]";
    private static final String STRING_NULL = "null";
    private static final String STRING_ERROR_JSON = "{error: %s}";
    private static final String STRING_MASK = "******";
    private static final String UNKNOWN = "unknown";
    private static final String X_USER_ID_HEADER_NAME = "X-User-ID";
    private static final String START_BRACKET = "{";
    private static final String END_BRACKET = "}";
    private static final String QUOTE = "\"";
    private static final String COLON = ":";
    private static final String COMMA = ",";

    public SerializationService() {
        this.track4jObjectMapper = track4jObjectMapper();
        this.sensitiveHttpHeaders = new HttpHeaders();
        sensitiveHttpHeaders.addHeader(new HttpHeader("authorization"));
        sensitiveHttpHeaders.addHeader(new HttpHeader("cookie"));
        sensitiveHttpHeaders.addHeader(new HttpHeader("x-api-key"));
        sensitiveHttpHeaders.addHeader(new HttpHeader("x-auth-token"));

        this.ipHttpHeaders = new HttpHeaders();
        ipHttpHeaders.addHeader(new HttpHeader("X-Forwarded-For"));
        ipHttpHeaders.addHeader(new HttpHeader("X-Real-IP"));
        ipHttpHeaders.addHeader(new HttpHeader("Proxy-Client-IP"));
        ipHttpHeaders.addHeader(new HttpHeader("WL-Proxy-Client-IP"));
        ipHttpHeaders.addHeader(new HttpHeader("HTTP_CLIENT_IP"));
        ipHttpHeaders.addHeader(new HttpHeader("HTTP_X_FORWARDED_FOR"));
    }

    public String serializeArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return STRING_EMPTY_ARRAY;
        }

        try {
            return track4jObjectMapper.writeValueAsString(args);
        } catch (Exception e) {
            return String.format(STRING_ERROR_ARRAY, e.getMessage());
        }
    }

    public String serializeResult(Object result) {
        if (result == null) {
            return STRING_NULL;
        }

        try {
            return track4jObjectMapper.writeValueAsString(result);
        } catch (Exception e) {
            return String.format(STRING_ERROR_JSON, e.getMessage());
        }
    }

    public String getHeadersAsJson(LightweightRequestWrapper request) {
        StringBuilder json = new StringBuilder(START_BRACKET);
        Enumeration<String> headerNames = request.getHeaderNames();

        while (headerNames.asIterator().hasNext()) {
            String name = headerNames.nextElement();
            String value = maskSensitiveHeader(name, request.getHeader(name));
            json.append(QUOTE)
                    .append(name)
                    .append(QUOTE)
                    .append(COLON)
                    .append(QUOTE)
                    .append(value)
                    .append(QUOTE);
            if (headerNames.asIterator().hasNext()) {
                json.append(COMMA);
            }
        }

        json.append(END_BRACKET);
        return json.toString();
    }

    public String getHeadersAsJson(LightweightResponseWrapper response) {
        StringBuilder json = new StringBuilder(START_BRACKET);
        Iterator<String> it = response.getHeaderNames().iterator();

        while (it.hasNext()) {
            String name = it.next();
            String value = maskSensitiveHeader(name, response.getHeader(name));
            json.append(QUOTE)
                    .append(name)
                    .append(QUOTE)
                    .append(COLON)
                    .append(QUOTE)
                    .append(value)
                    .append(QUOTE);
            if (it.hasNext()) {
                json.append(COMMA);
            }
        }

        json.append(END_BRACKET);
        return json.toString();
    }

    private String maskSensitiveHeader(String name, String value) {
        if (value == null) return "";
        if (sensitiveHttpHeaders.contains(new HttpHeader(name.toLowerCase()))) {
            return STRING_MASK;
        }
        return value;
    }

    public String getRequestBody(LightweightRequestWrapper request) {
        byte[] content = request.getContentAsByteArray();
        if (content.length > 0) {
            return new String(content, StandardCharsets.UTF_8);
        }
        return null;
    }

    public String getResponseBody(LightweightResponseWrapper response) {
        byte[] content = response.getContentAsByteArray();
        if (content.length > 0) {
            return new String(content, StandardCharsets.UTF_8);
        }
        return null;
    }

    public String getClientIp(HttpServletRequest request) {
        for (HttpHeader httpHeader : ipHttpHeaders) {
            String ip = request.getHeader(httpHeader.getHeaderName());
            if (StringUtils.hasText(ip) && !UNKNOWN.equalsIgnoreCase(ip)) {
                int commaIndex = ip.indexOf(',');
                if (commaIndex > 0) {
                    return ip.substring(0, commaIndex).trim();
                }
                return ip.trim();
            }
        }

        return request.getRemoteAddr();
    }

    public String extractUserId(HttpServletRequest request) {
        if (request.getUserPrincipal() != null) {
            return request.getUserPrincipal().getName();
        }

        String userId = request.getHeader(X_USER_ID_HEADER_NAME);
        if (StringUtils.hasText(userId)) {
            return userId;
        }

        return null;
    }

    public static ObjectMapper track4jObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
}