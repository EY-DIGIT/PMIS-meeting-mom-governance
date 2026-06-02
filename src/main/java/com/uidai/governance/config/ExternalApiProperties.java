package com.uidai.governance.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration for the external Python services (User &amp; Activity).
 * Bound from the {@code external.*} keys in {@code application.yml}.
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
            int readTimeoutMs
    ) {
        public ServiceConfig {
            if (connectTimeoutMs <= 0) {
                connectTimeoutMs = 3000;
            }
            if (readTimeoutMs <= 0) {
                readTimeoutMs = 5000;
            }
        }
    }
}
