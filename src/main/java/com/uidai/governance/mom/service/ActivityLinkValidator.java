package com.uidai.governance.mom.service;

import com.uidai.governance.common.exception.BusinessValidationException;
import com.uidai.governance.common.exception.ExternalServiceException;
import com.uidai.governance.external.activity.ActivityServiceClient;
import com.uidai.governance.external.activity.dto.UpdateActivityRequest;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Validates and synchronizes the link between MoM action items and external
 * project activities (per the chosen integration: action items reference an
 * activity id; supports MEET-FR-05.2).
 */
@Component
public class ActivityLinkValidator {

    private static final Logger log = LoggerFactory.getLogger(ActivityLinkValidator.class);

    private final ActivityServiceClient activityServiceClient;

    public ActivityLinkValidator(ActivityServiceClient activityServiceClient) {
        this.activityServiceClient = activityServiceClient;
    }

    /**
     * Verifies that a linked activity exists. No-op for a null/blank id, or when
     * the activity service is disabled (so an unavailable service never blocks a
     * governance action). Throws if the service is enabled and the activity is
     * not found.
     */
    public void validateExists(String activityId) {
        if (activityId == null || activityId.isBlank() || !activityServiceClient.isEnabled()) {
            return;
        }
        activityServiceClient.getActivity(activityId)
                .orElseThrow(() -> new BusinessValidationException("Linked activity not found: " + activityId));
    }

    /**
     * Best-effort sync that records the activity as completed when its linked
     * action item is closed. Failures are logged, never propagated, so action
     * item completion is not coupled to activity-service availability.
     */
    public void syncCompleted(String activityId) {
        if (activityId == null || activityId.isBlank() || !activityServiceClient.isEnabled()) {
            return;
        }
        try {
            activityServiceClient.updateActivity(activityId, UpdateActivityRequest.markCompleted(Instant.now()));
        } catch (ExternalServiceException ex) {
            log.warn("Could not sync completion to activity {}: {}", activityId, ex.getMessage());
        }
    }
}
