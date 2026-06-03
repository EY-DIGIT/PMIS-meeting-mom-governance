package com.uidai.governance.config;

import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration for the external Python services (User &amp; Activity).
 * Bound from the {@code external.*} keys in {@code application.yml}.
 *
 * <p>Endpoint paths are configured under {@code external.<service>.endpoints.*}
 * rather than hardcoded in the REST clients, so they can be changed without a
 * rebuild.</p>
 */
@ConfigurationProperties(prefix = "external")
public record ExternalApiProperties(
        ServiceConfig userService,
        ServiceConfig activityService
) {
    public record ServiceConfig(
            String baseUrl,
            boolean enabled,
            int connectTimeoutMs,
            int readTimeoutMs,
            Map<String, String> endpoints
    ) {
        public ServiceConfig {
            if (connectTimeoutMs <= 0) {
                connectTimeoutMs = 3000;
            }
            if (readTimeoutMs <= 0) {
                readTimeoutMs = 5000;
            }
            endpoints = endpoints == null ? Map.of() : Map.copyOf(endpoints);
        }

        /**
         * Resolves a configured endpoint path template by key
         * (e.g. {@code "get-user"} &rarr; {@code "/users/{id}"}), failing fast
         * when the corresponding {@code external.<service>.endpoints.<key>}
         * property is missing.
         */
        public String endpoint(String key) {
            String path = endpoints.get(key);
            if (path == null || path.isBlank()) {
                throw new IllegalStateException(
                        "Missing endpoint configuration 'external.*.endpoints." + key + "'");
            }
            return path;
        }
    }
}
