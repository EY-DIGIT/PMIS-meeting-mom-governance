package com.uidai.governance.config;

import java.time.Duration;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.ClientHttpRequestFactorySettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ClientHttpRequestInitializer;
import org.springframework.web.client.RestClient;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Builds named {@link RestClient} beans for the two external Python services.
 * Base URLs and timeouts come from {@link ExternalApiProperties}.
 *
 * <p>Both clients propagate the caller's bearer token: the {@code Authorization}
 * header of the inbound HTTP request is forwarded as-is on every outbound call to
 * the User and Activity services, so authentication for all external APIs flows
 * through the standard header rather than request bodies.</p>
 */
@Configuration
public class RestClientConfig {

    public static final String USER_CLIENT = "userRestClient";
    public static final String ACTIVITY_CLIENT = "activityRestClient";

    private final ExternalApiProperties properties;

    public RestClientConfig(ExternalApiProperties properties) {
        this.properties = properties;
    }

    @Bean(USER_CLIENT)
    public RestClient userRestClient() {
        return build(properties.userService());
    }

    @Bean(ACTIVITY_CLIENT)
    public RestClient activityRestClient() {
        return build(properties.activityService());
    }

    private RestClient build(ExternalApiProperties.ServiceConfig cfg) {
        ClientHttpRequestFactorySettings settings = ClientHttpRequestFactorySettings.defaults()
                .withConnectTimeout(Duration.ofMillis(cfg.connectTimeoutMs()))
                .withReadTimeout(Duration.ofMillis(cfg.readTimeoutMs()));
        return RestClient.builder()
                .baseUrl(cfg.baseUrl())
                .requestFactory(ClientHttpRequestFactoryBuilder.detect().build(settings))
                .requestInitializer(bearerTokenPropagation())
                .build();
    }

    /**
     * Copies the inbound request's {@code Authorization} header onto every outbound
     * call so the caller's bearer token authenticates the downstream service. No-op
     * when there is no active request (e.g. scheduled jobs) or no token present.
     */
    private static ClientHttpRequestInitializer bearerTokenPropagation() {
        return request -> {
            if (request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                return;
            }
            if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs) {
                String authorization = attrs.getRequest().getHeader(HttpHeaders.AUTHORIZATION);
                if (authorization != null && !authorization.isBlank()) {
                    request.getHeaders().set(HttpHeaders.AUTHORIZATION, authorization);
                }
            }
        };
    }
}
