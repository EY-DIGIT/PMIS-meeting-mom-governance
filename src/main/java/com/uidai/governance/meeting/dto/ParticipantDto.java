package com.uidai.governance.meeting.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.uidai.governance.meeting.domain.MeetingParticipant;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * A meeting participant. {@code userId} is validated against the external User
 * service (MEET-FR-03.2). {@code isPresent} records attendance - it can be set
 * when creating the meeting and updated when editing it.
 */
public record ParticipantDto(
        @NotBlank @Size(max = 100) String userId,
        @Size(max = 100) String participantRole,
        boolean mandatory,
        @JsonProperty("isPresent") boolean isPresent
) {
    public static ParticipantDto from(MeetingParticipant p) {
        return new ParticipantDto(p.getUserId(), p.getParticipantRole(), p.isMandatory(), p.isPresent());
    }
}
