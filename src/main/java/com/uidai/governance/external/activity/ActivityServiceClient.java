package com.uidai.governance.external.activity;

import com.uidai.governance.external.activity.dto.ActivityDto;
import com.uidai.governance.external.activity.dto.UpdateActivityRequest;
import java.util.Optional;

/**
 * Abstraction over the external (Python) project <em>Activities</em> service
 * ({@code /projects/api/v3/activities}).
 *
 * <p>Project activities are the scheduled units of project work (with dates,
 * status, owner division, vendor and dependencies) that meetings and action
 * items in this module can reference.</p>
 */
public interface ActivityServiceClient {

    /**
     * Fetches a project activity by id
     * ({@code GET /projects/api/v3/activities/{id}}). Returns empty if not found.
     */
    Optional<ActivityDto> getActivity(String activityId);

    /**
     * Partially updates a project activity
     * ({@code PATCH /projects/api/v3/activities/{id}}). Only the non-null fields
     * of {@code request} are sent. Returns the updated activity.
     */
    ActivityDto updateActivity(String activityId, UpdateActivityRequest request);

    /**
     * Whether the activity service integration is enabled. Callers use this to
     * skip activity validation/sync when the service is intentionally disabled
     * (local dev / tests) rather than treating "no response" as "not found".
     */
    boolean isEnabled();
}
