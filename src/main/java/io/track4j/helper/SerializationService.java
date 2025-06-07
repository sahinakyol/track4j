package io.track4j.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.track4j.objects.LightweightRequestWrapper;
import io.track4j.objects.LightweightResponseWrapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.Iterator;

public final class SerializationService {

    private static final ObjectMapper track4jObjectMapper = track4jObjectMapper();
    private final HttpHeaders ipHttpHeaders;
    private static final String STRING_EMPTY_ARRAY = "[]";
    private static final String STRING_ERROR_ARRAY = "[Error: %s]";
    private static final String STRING_NULL = "null";
    private static final String STRING_ERROR_JSON = "{error: %s}";
    private static final String UNKNOWN = "unknown";
    private static final String X_USER_ID_HEADER_NAME = "X-User-ID";
    private static final String START_BRACKET = "{";
    private static final String END_BRACKET = "}";
    private static final String QUOTE = "\"";
    private static final String COLON = ":";
    private static final String COMMA = ",";
    private static final ThreadLocal<StringBuilder> stringBuilder = ThreadLocal.withInitial(StringBuilder::new);

    public SerializationService() {
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
        StringBuilder json = stringBuilder.get();
        json.setLength(0);
        json.append(START_BRACKET);
        Iterator<String> headerNames = request.getHeaderNames().asIterator();

        while (headerNames.hasNext()) {
            String name = headerNames.next();
            json.append(QUOTE)
                    .append(name)
                    .append(QUOTE)
                    .append(COLON)
                    .append(QUOTE)
                    .append(request.getHeader(name))
                    .append(QUOTE);
            if (headerNames.hasNext()) {
                json.append(COMMA);
            }
        }

        json.append(END_BRACKET);
        return json.toString();
    }

    public String getHeadersAsJson(LightweightResponseWrapper response) {
        StringBuilder json = stringBuilder.get();
        json.setLength(0);
        json.append(START_BRACKET);
        Iterator<String> it = response.getHeaderNames().iterator();

        while (it.hasNext()) {
            String name = it.next();
            json.append(QUOTE)
                    .append(name)
                    .append(QUOTE)
                    .append(COLON)
                    .append(QUOTE)
                    .append(response.getHeader(name))
                    .append(QUOTE);
            if (it.hasNext()) {
                json.append(COMMA);
            }
        }

        json.append(END_BRACKET);
        return json.toString();
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