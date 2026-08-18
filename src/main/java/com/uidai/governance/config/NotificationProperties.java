package com.uidai.governance.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration for the external notification (email) API, bound from the
 * {@code app.notification.*} keys in {@code application.properties}.
 *
 * <p>Unlike the User and Activity services this endpoint is configured as a
 * single absolute {@code url} rather than a base URL plus endpoint paths, and it
 * authenticates with its own service token ({@code auth-token}) instead of the
 * caller's bearer token. When the token is left blank the caller's inbound
 * {@code Authorization} header is forwarded instead.</p>
 */
@ConfigurationProperties(prefix = "app.notification")
public record NotificationProperties(
        boolean enabled,
        String url,
        String authToken,
        int connectTimeoutMs,
        int readTimeoutMs
) {
    public NotificationProperties {
        if (connectTimeoutMs <= 0) {
            connectTimeoutMs = 3000;
        }
        if (readTimeoutMs <= 0) {
            readTimeoutMs = 5000;
        }
    }

    /** True when a service token is configured and should be sent instead of the caller's. */
    public boolean hasAuthToken() {
        return authToken != null && !authToken.isBlank();
    }

    /**
     * The value for the outbound {@code Authorization} header. The configured
     * token is sent as-is when it already carries a scheme (e.g. {@code Bearer x},
     * {@code Basic x}); otherwise it is prefixed with {@code Bearer }.
     */
    public String authorizationHeader() {
        String token = authToken.trim();
        return token.contains(" ") ? token : "Bearer " + token;
    }
}
