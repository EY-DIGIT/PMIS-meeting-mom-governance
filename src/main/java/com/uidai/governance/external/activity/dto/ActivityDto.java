package com.uidai.governance.external.activity.dto;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;
import java.util.List;

/**
 * A project activity as returned by the external Python project service
 * ({@code GET /projects/api/v3/activities/{id}} and the create endpoint).
 *
 * <p>Unknown fields returned by the service are ignored (Jackson is configured
 * to not fail on unknown properties), so the service can evolve without breaking
 * this client. {@code attachments} is captured verbatim (the stored attachment
 * metadata PMIS returns: id, filename, url, etc.).</p>
 */
public record ActivityDto(
        String id,
        String displayCode,
        String projectId,
        String milestoneId,
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
        List<String> dependsOn,
        JsonNode attachments,
        JsonNode comment
) {
}
