package com.uidai.governance.meeting.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.uidai.governance.meeting.domain.MeetingParticipant;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * A meeting participant (internal attendee). {@code userId} is validated against
 * the external User service (MEET-FR-03.2). {@code isPresent} records attendance -
 * it can be set when creating the meeting and updated when editing it.
 *
 * <p>{@code email} is mandatory and is the address the meeting invite is sent
 * to; {@code roleName} is optional and is stored for display only.</p>
 */
public record ParticipantDto(
        @NotBlank @Size(max = 100)
        @Schema(description = "User id from the User service", example = "ead6aaed-6be2-4c23-a73a-65eca06b3fee")
        String userId,
        @NotBlank @Email @Size(max = 150)
        @Schema(description = "Email address the meeting invite is sent to (mandatory)",
                example = "attendee@uidai.gov.in")
        String email,
        @Size(max = 150)
        @Schema(description = "Role name of the attendee", example = "Project Manager")
        String roleName,
        @Size(max = 100) @Schema(description = "Role at the meeting", example = "Chair")
        String participantRole,
        @Schema(description = "Whether attendance is mandatory", example = "true")
        boolean mandatory,
        @JsonProperty("isPresent")
        @Schema(description = "Whether the attendee was present (recorded on update)", example = "false")
        boolean isPresent
) {
    public static ParticipantDto from(MeetingParticipant p) {
        return new ParticipantDto(p.getUserId(), p.getEmail(), p.getRoleName(),
                p.getParticipantRole(), p.isMandatory(), p.isPresent());
    }
}
