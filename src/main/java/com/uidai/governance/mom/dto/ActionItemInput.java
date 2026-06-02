package com.uidai.governance.mom.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/**
 * Input for creating an action item, either standalone or as part of a MoM
 * (MEET-FR-04.2). Optional links to project artefacts satisfy MEET-FR-05.2.
 */
public record ActionItemInput(
        @NotBlank @Size(max = 4000) String description,
        @NotBlank @Size(max = 100) String assignedToUserId,
        LocalDate dueDate,
        Long linkedTaskId,
        Long linkedMilestoneId,
        @Size(max = 100) String linkedTicketId,
        @Size(max = 100) String linkedActivityId
) {
}
