package com.uidai.governance.meeting.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;

/**
 * Request to update an existing meeting's mutable fields.
 */
public record UpdateMeetingRequest(
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
