package com.uidai.governance.external.activity.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Request body for creating a project activity
 * ({@code POST /projects/api/v3/milestones/{milestoneId}/activities/create}).
 *
 * <p>Field shape mirrors the external Python project service. Null fields and
 * empty collections are sent as-is (the service accepts explicit nulls), so the
 * serialized body matches the documented payload.</p>
 */
public record CreateActivityRequest(
        String name,
        String description,
        OffsetDateTime startDate,
        OffsetDateTime endDate,
        OffsetDateTime actualStartDate,
        OffsetDateTime actualEndDate,
        String status,
        Boolean activityStarted,
        String priority,
        Integer position,
        String ownerDivision,
        String ownerDivisionOther,
        List<String> concernedDivision,
        String concernedDivisionOther,
        String vendorId,
        List<String> dependsOn,
        String category,
        BigDecimal ccnValue
) {
}
