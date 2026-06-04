package com.uidai.governance.external.activity;

import com.uidai.governance.external.activity.dto.ActivityDto;
import com.uidai.governance.external.activity.dto.CreateActivityRequest;
import com.uidai.governance.external.activity.dto.ProjectDto;
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
     * Creates a project activity under a milestone
     * ({@code POST /projects/api/v3/milestones/{milestoneId}/activities/create}).
     *
     * <p>Called when a meeting is recorded, so the meeting is linked to a project
     * activity. The caller's bearer token is forwarded from the inbound request
     * {@code Authorization} header automatically. Returns the created activity
     * (unwrapped from the service's {@code data} envelope).</p>
     *
     * @param milestoneId the milestone the activity is created under (URL path)
     * @param request     the activity payload
     */
    ActivityDto createActivity(String milestoneId, CreateActivityRequest request);

    /**
     * Fetches a project activity by id
     * ({@code GET /projects/api/v3/activities/{id}}). Returns empty if not found.
     */
    Optional<ActivityDto> getActivity(String activityId);

    /**
     * Fetches a project by id ({@code GET /projects/api/v3/projects/{id}}).
     * Used at meeting creation to resolve the project's {@code meetingMilestoneId},
     * under which the meeting's activity is created. Returns empty if not found.
     */
    Optional<ProjectDto> getProject(String projectId);

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
