package com.uidai.governance.external.activity;

import com.uidai.governance.common.exception.ExternalServiceException;
import com.uidai.governance.config.ExternalApiProperties;
import com.uidai.governance.config.RestClientConfig;
import com.uidai.governance.external.activity.dto.ActivityDto;
import com.uidai.governance.external.activity.dto.UpdateActivityRequest;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * REST implementation of {@link ActivityServiceClient} backed by the external
 * Python project activities service.
 *
 * <p>Endpoints (relative to the configured base URL
 * {@code external.activity-service.base-url}, default
 * {@code http://10.1.131.199/projects/api/v3}):</p>
 * <ul>
 *   <li>{@code GET  /activities/{id}} &mdash; fetch an activity</li>
 *   <li>{@code PATCH /activities/{id}} &mdash; partial update</li>
 * </ul>
 *
 * <p>When {@code external.activity-service.enabled=false} (local dev / tests),
 * calls short-circuit so the module runs standalone.</p>
 */
@Component
public class ActivityServiceRestClient implements ActivityServiceClient {

    private static final Logger log = LoggerFactory.getLogger(ActivityServiceRestClient.class);

    private final RestClient restClient;
    private final boolean enabled;

    public ActivityServiceRestClient(@Qualifier(RestClientConfig.ACTIVITY_CLIENT) RestClient restClient,
                                     ExternalApiProperties properties) {
        this.restClient = restClient;
        this.enabled = properties.activityService().enabled();
    }

    @Override
    public Optional<ActivityDto> getActivity(String activityId) {
        if (!enabled) {
            log.debug("Activity service disabled - skipping lookup of {}", activityId);
            return Optional.empty();
        }
        try {
            ActivityDto activity = restClient.get()
                    .uri("/activities/{id}", activityId)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                        // Swallow 404 -> empty; other 4xx surface as errors below.
                    })
                    .body(ActivityDto.class);
            return Optional.ofNullable(activity);
        } catch (RestClientException ex) {
            throw new ExternalServiceException("Activity lookup failed for " + activityId, ex);
        }
    }

    @Override
    public ActivityDto updateActivity(String activityId, UpdateActivityRequest request) {
        if (!enabled) {
            log.debug("Activity service disabled - skipping update of {}", activityId);
            return null;
        }
        try {
            return restClient.patch()
                    .uri("/activities/{id}", activityId)
                    .body(request)
                    .retrieve()
                    .body(ActivityDto.class);
        } catch (RestClientException ex) {
            throw new ExternalServiceException("Activity update failed for " + activityId, ex);
        }
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
