package com.uidai.governance.mom.dto;

import com.uidai.governance.mom.domain.ActionItemStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/**
 * Request to update an action item's mutable fields. The due date is NOT changed
 * here directly when an extension workflow applies; use the extension endpoints
 * (MEET-FR-04.5) for governed date changes.
 */
public record UpdateActionItemRequest(
        @NotBlank @Size(max = 4000) String description,
        @NotBlank @Size(max = 100) String assignedToUserId,
        @NotNull ActionItemStatus status,
        Long linkedTaskId,
        Long linkedMilestoneId,
        @Size(max = 100) String linkedTicketId,
        @Size(max = 100) String linkedActivityId
) {
}
