package com.uidai.governance.mom.dto;

import com.uidai.governance.mom.domain.ActionItem;
import com.uidai.governance.mom.domain.ActionItemStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/** Full action item representation (MEET-FR-04.2, .4, .5; MEET-FR-05.2). */
public record ActionItemResponse(
        Long id,
        Long momId,
        String description,
        String assignedToUserId,
        LocalDate dueDate,
        ActionItemStatus status,
        Long linkedTaskId,
        Long linkedMilestoneId,
        String linkedTicketId,
        String linkedActivityId,
        List<CommentDto> comments,
        List<ExtensionRequestResponse> extensionRequests,
        String createdBy,
        Instant createdAt,
        String updatedBy,
        Instant updatedAt
) {
    public static ActionItemResponse from(ActionItem a) {
        return new ActionItemResponse(
                a.getId(),
                a.getMom().getId(),
                a.getDescription(),
                a.getAssignedToUserId(),
                a.getDueDate(),
                a.getStatus(),
                a.getLinkedTaskId(),
                a.getLinkedMilestoneId(),
                a.getLinkedTicketId(),
                a.getLinkedActivityId(),
                a.getComments().stream().map(CommentDto::from).toList(),
                a.getExtensionRequests().stream().map(ExtensionRequestResponse::from).toList(),
                a.getCreatedBy(),
                a.getCreatedAt(),
                a.getUpdatedBy(),
                a.getUpdatedAt());
    }
}
