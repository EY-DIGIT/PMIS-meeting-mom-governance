package com.uidai.governance.external.activity.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.List;

/**
 * Partial-update body for {@code PATCH /projects/api/v3/activities/{id}}.
 *
 * <p>Only non-null fields are serialized ({@code JsonInclude.NON_NULL}), so a
 * caller can update a single attribute (e.g. just {@code status} or
 * {@code actualEndDate}) without overwriting the rest.</p>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record UpdateActivityRequest(
        String name,
        String description,
        Instant startDate,
        Instant endDate,
        Instant actualStartDate,
        Instant actualEndDate,
        String status,
        Boolean activityStarted,
        String priority,
        String ownerDivision,
        String ownerDivisionOther,
        List<String> concernedDivision,
        String concernedDivisionOther,
        String vendorId,
        Integer position,
        List<String> dependsOn
) {
    /**
     * Builds a partial update that records an activity as finished: sets
     * {@code actualEndDate} and flags {@code activityStarted}. All other fields
     * are left null and therefore not sent.
     */
    public static UpdateActivityRequest markCompleted(Instant actualEndDate) {
        return new UpdateActivityRequest(null, null, null, null, null, actualEndDate,
                null, Boolean.TRUE, null, null, null, null, null, null, null, null);
    }
}
