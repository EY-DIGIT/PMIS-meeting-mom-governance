package com.uidai.governance.meeting.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.uidai.governance.meeting.domain.MeetingParticipant;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * A meeting participant (internal attendee). {@code userId} is validated against
 * the external User service (MEET-FR-03.2). {@code isPresent} records attendance -
 * it can be set when creating the meeting and updated when editing it.
 */
public record ParticipantDto(
        @NotBlank @Size(max = 100)
        @Schema(description = "User id from the User service", example = "ead6aaed-6be2-4c23-a73a-65eca06b3fee")
        String userId,
        @Size(max = 100) @Schema(description = "Role at the meeting", example = "Chair")
        String participantRole,
        @Schema(description = "Whether attendance is mandatory", example = "true")
        boolean mandatory,
        @JsonProperty("isPresent")
        @Schema(description = "Whether the attendee was present (recorded on update)", example = "false")
        boolean isPresent
) {
    public static ParticipantDto from(MeetingParticipant p) {
        return new ParticipantDto(p.getUserId(), p.getParticipantRole(), p.isMandatory(), p.isPresent());
    }
}
