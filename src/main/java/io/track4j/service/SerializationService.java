package io.track4j.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

public class SerializationService {

    private final ObjectMapper track4jObjectMapper;
    private static final List<String> SENSITIVE_HEADERS = Arrays.asList(
            "authorization", "cookie", "x-api-key", "x-auth-token"
    );

    public SerializationService() {
        this.track4jObjectMapper = track4jObjectMapper();
    }

    public String serializeArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return "[]";
        }

        try {
            return track4jObjectMapper.writeValueAsString(args);
        } catch (Exception e) {
            return String.format("[\"Error: %s\"]", e.getMessage());
        }
    }

    public String serializeResult(Object result) {
        if (result == null) {
            return "null";
        }

        try {
            return track4jObjectMapper.writeValueAsString(result);
        } catch (Exception e) {
            return String.format("{\"error\":\"%s\"}", e.getMessage());
        }
    }

    public String getHeadersAsJson(ContentCachingRequestWrapper request) {
        StringBuilder json = new StringBuilder("{");
        Enumeration<String> headerNames = request.getHeaderNames();
        boolean first = true;

        while (headerNames.hasMoreElements()) {
            if (!first) json.append(",");
            String name = headerNames.nextElement();
            String value = maskSensitiveHeader(name, request.getHeader(name));
            json.append("\"").append(name).append("\":\"").append(value).append("\"");
            first = false;
        }

        json.append("}");
        return json.toString();
    }

    public String getHeadersAsJson(ContentCachingResponseWrapper response) {
        StringBuilder json = new StringBuilder("{");
        boolean first = true;

        for (String name : response.getHeaderNames()) {
            if (!first) json.append(",");
            String value = maskSensitiveHeader(name, response.getHeader(name));
            json.append("\"").append(name).append("\":\"").append(value).append("\"");
            first = false;
        }

        json.append("}");
        return json.toString();
    }

    private String maskSensitiveHeader(String name, String value) {
        if (value == null) return "";
        if (SENSITIVE_HEADERS.contains(name.toLowerCase())) {
            return "******";
        }
        return value;
    }

    public String getRequestBody(ContentCachingRequestWrapper request) {
        byte[] content = request.getContentAsByteArray();
        if (content.length > 0) {
            return new String(content, StandardCharsets.UTF_8);
        }
        return null;
    }

    public String getResponseBody(ContentCachingResponseWrapper response) {
        byte[] content = response.getContentAsByteArray();
        if (content.length > 0) {
            return new String(content, StandardCharsets.UTF_8);
        }
        return null;
    }

    public String getClientIp(HttpServletRequest request) {
        String[] headerNames = {"X-Forwarded-For", "X-Real-IP", "Proxy-Client-IP",
                "WL-Proxy-Client-IP", "HTTP_CLIENT_IP", "HTTP_X_FORWARDED_FOR"};

        for (String headerName : headerNames) {
            String ip = request.getHeader(headerName);
            if (StringUtils.hasText(ip) && !"unknown".equalsIgnoreCase(ip)) {
                return ip.split(",")[0].trim();
            }
        }

        return request.getRemoteAddr();
    }

    public String extractUserId(HttpServletRequest request) {
        if (request.getUserPrincipal() != null) {
            return request.getUserPrincipal().getName();
        }

        String userId = request.getHeader("X-User-ID");
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