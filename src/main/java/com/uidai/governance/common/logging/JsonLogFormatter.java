package com.uidai.governance.common.logging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Serializes request payloads to compact JSON for log lines, so external-API
 * requests are recorded in a structured, parseable form. Uses the application's
 * configured {@link ObjectMapper}; on failure it falls back to {@code toString()}
 * so logging never breaks a request.
 */
@Component
public class JsonLogFormatter {

    private static final Logger log = LoggerFactory.getLogger(JsonLogFormatter.class);

    private final ObjectMapper objectMapper;

    public JsonLogFormatter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /** Renders {@code payload} as a JSON string for logging. */
    public String toJson(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            log.warn("Failed to serialize payload to JSON for logging: {}", ex.getMessage());
            return String.valueOf(payload);
        }
    }
}
