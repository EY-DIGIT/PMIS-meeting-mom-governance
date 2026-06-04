package com.uidai.governance.external.activity.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * A project as returned by the external Python project service
 * ({@code GET /projects/api/v3/projects/{id}}), inside the {@code data} envelope.
 *
 * <p>Only the fields this module needs are mapped; {@link #meetingMilestoneId()}
 * is the milestone under which a meeting's activity is created. Field names are
 * camelCase, matching the service's JSON. Unknown fields are ignored.</p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ProjectDto(
        String id,
        String projectCode,
        String name,
        String meetingMilestoneId
) {
}
