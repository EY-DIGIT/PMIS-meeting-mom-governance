package com.uidai.governance.external.activity;

import com.uidai.governance.common.exception.ExternalApiException;
import com.uidai.governance.common.exception.ExternalServiceException;
import com.uidai.governance.common.logging.JsonLogFormatter;
import com.uidai.governance.config.ExternalApiProperties;
import com.uidai.governance.config.RestClientConfig;
import com.uidai.governance.external.activity.dto.ActivityDto;
import com.uidai.governance.external.activity.dto.ActivityEnvelope;
import com.uidai.governance.external.activity.dto.CreateActivityRequest;
import com.uidai.governance.external.activity.dto.ProjectDto;
import com.uidai.governance.external.activity.dto.ProjectEnvelope;
import com.uidai.governance.external.activity.dto.UpdateActivityRequest;
import java.util.Map;
import java.util.Optional;
import org.springframework.http.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
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

    private static final String EP_CREATE_ACTIVITY = "create-activity";
    private static final String EP_GET_ACTIVITY = "get-activity";
    private static final String EP_UPDATE_ACTIVITY = "update-activity";
    private static final String EP_GET_PROJECT = "get-project";

    private final RestClient restClient;
    private final ExternalApiProperties.ServiceConfig config;
    private final JsonLogFormatter jsonLog;
    private final boolean enabled;

    public ActivityServiceRestClient(@Qualifier(RestClientConfig.ACTIVITY_CLIENT) RestClient restClient,
                                     ExternalApiProperties properties,
                                     JsonLogFormatter jsonLog) {
        this.restClient = restClient;
        this.config = properties.activityService();
        this.jsonLog = jsonLog;
        this.enabled = config.enabled();
    }

    @Override
    public ActivityDto createActivity(String milestoneId, CreateActivityRequest request) {
        if (!enabled) {
            log.debug("Activity service disabled - skipping activity creation under milestone {}", milestoneId);
            return null;
        }
        String path = config.endpoint(EP_CREATE_ACTIVITY);
        log.info("Calling Activity service: POST {}{} [milestoneId={}] request={}",
                config.baseUrl(), path, milestoneId, jsonLog.toJson(request));
        try {
            ActivityEnvelope envelope = restClient.post()
                    .uri(path, milestoneId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(ActivityEnvelope.class);
            if (envelope == null || envelope.data() == null) {
                throw new ExternalServiceException(
                        "Activity creation under milestone " + milestoneId + " returned no data");
            }
            return envelope.data();
        } catch (RestClientException ex) {
            log.error("Activity service call failed: POST {}{} [milestoneId={}] request={}",
                    config.baseUrl(), path, milestoneId, jsonLog.toJson(request), ex);
            throw translate(ex, "Activity creation failed under milestone " + milestoneId);
        }
    }

    @Override
    public Optional<ActivityDto> getActivity(String activityId) {
        if (!enabled) {
            log.debug("Activity service disabled - skipping lookup of {}", activityId);
            return Optional.empty();
        }
        String path = config.endpoint(EP_GET_ACTIVITY);
        log.info("Calling Activity service: GET {}{} request={}",
                config.baseUrl(), path, jsonLog.toJson(Map.of("activityId", activityId)));
        try {
            ActivityDto activity = restClient.get()
                    .uri(path, activityId)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                        // Swallow 404 -> empty; other 4xx surface as errors below.
                    })
                    .body(ActivityDto.class);
            return Optional.ofNullable(activity);
        } catch (RestClientException ex) {
            log.error("Activity service call failed: GET {}{} request={}",
                    config.baseUrl(), path, jsonLog.toJson(Map.of("activityId", activityId)), ex);
            throw translate(ex, "Activity lookup failed for " + activityId);
        }
    }

    @Override
    public Optional<ProjectDto> getProject(String projectId) {
        if (!enabled) {
            log.debug("Activity service disabled - skipping project lookup of {}", projectId);
            return Optional.empty();
        }
        String path = config.endpoint(EP_GET_PROJECT);
        log.info("Calling Activity service: GET {}{} request={}",
                config.baseUrl(), path, jsonLog.toJson(Map.of("projectId", projectId)));
        try {
            ProjectEnvelope envelope = restClient.get()
                    .uri(path, projectId)
                    .retrieve()
                    .body(ProjectEnvelope.class);
            return Optional.ofNullable(envelope).map(ProjectEnvelope::data);
        } catch (RestClientException ex) {
            // Surface the project service's own error (status + body) to the caller.
            log.error("Activity service call failed: GET {}{} request={}",
                    config.baseUrl(), path, jsonLog.toJson(Map.of("projectId", projectId)), ex);
            throw translate(ex, "Project lookup failed for " + projectId);
        }
    }

    @Override
    public ActivityDto updateActivity(String activityId, UpdateActivityRequest request) {
        if (!enabled) {
            log.debug("Activity service disabled - skipping update of {}", activityId);
            return null;
        }
        String path = config.endpoint(EP_UPDATE_ACTIVITY);
        log.info("Calling Activity service: PATCH {}{} [activityId={}] request={}",
                config.baseUrl(), path, activityId, jsonLog.toJson(request));
        try {
            return restClient.patch()
                    .uri(path, activityId)
                    .body(request)
                    .retrieve()
                    .body(ActivityDto.class);
        } catch (RestClientException ex) {
            log.error("Activity service call failed: PATCH {}{} [activityId={}] request={}",
                    config.baseUrl(), path, activityId, jsonLog.toJson(request), ex);
            throw translate(ex, "Activity update failed for " + activityId);
        }
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Converts a RestClient failure into either an {@link ExternalApiException}
     * (carrying the external service's HTTP status and body, so it can be surfaced
     * to the caller as-is) for an error response, or an {@link ExternalServiceException}
     * (502) for connectivity/transport failures with no HTTP response.
     */
    private RuntimeException translate(RestClientException ex, String fallbackMessage) {
        if (ex instanceof HttpStatusCodeException httpEx) {
            return new ExternalApiException(httpEx.getStatusCode().value(), httpEx.getResponseBodyAsString());
        }
        return new ExternalServiceException(fallbackMessage, ex);
    }
}
