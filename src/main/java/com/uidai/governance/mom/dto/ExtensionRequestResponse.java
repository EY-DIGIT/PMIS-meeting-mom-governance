package com.uidai.governance.mom.dto;

import com.uidai.governance.mom.domain.ExtensionRequestStatus;
import com.uidai.governance.mom.domain.TimelineExtensionRequest;
import java.time.Instant;
import java.time.LocalDate;

/** A timeline extension request and its decision (MEET-FR-04.5). */
public record ExtensionRequestResponse(
        Long id,
        Long actionItemId,
        LocalDate previousDueDate,
        LocalDate requestedDueDate,
        String justification,
        ExtensionRequestStatus status,
        String requestedByUserId,
        Instant requestedAt,
        String decidedByUserId,
        String decisionComment,
        Instant decidedAt
) {
    public static ExtensionRequestResponse from(TimelineExtensionRequest r) {
        return new ExtensionRequestResponse(
                r.getId(),
                r.getActionItem().getId(),
                r.getPreviousDueDate(),
                r.getRequestedDueDate(),
                r.getJustification(),
                r.getStatus(),
                r.getRequestedByUserId(),
                r.getRequestedAt(),
                r.getDecidedByUserId(),
                r.getDecisionComment(),
                r.getDecidedAt());
    }
}
