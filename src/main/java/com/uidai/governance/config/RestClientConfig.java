package com.uidai.governance.config;

import java.time.Duration;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.ClientHttpRequestFactorySettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Builds named {@link RestClient} beans for the two external Python services.
 * Base URLs and timeouts come from {@link ExternalApiProperties}.
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
                .build();
    }
}
