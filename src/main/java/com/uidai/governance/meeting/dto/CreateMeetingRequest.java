package com.uidai.governance.meeting.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;

/**
 * Request to create a meeting. Mandatory fields are enforced via validation
 * (MEET-FR-03.1): type (selected at creation, MEET-FR-02.1), date, project,
 * agenda and at least one participant.
 */
public record CreateMeetingRequest(
        @NotBlank @Size(max = 250) String title,
        @NotBlank String meetingTypeCode,
        @NotNull Instant meetingDate,
        @NotNull Long projectId,
        Long stageId,
        Long serviceProviderId,
        @NotBlank @Size(max = 5000) String agenda,
        @NotEmpty @Valid List<ParticipantDto> participants
) {
}
