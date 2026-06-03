package com.uidai.governance.meeting.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.uidai.governance.meeting.domain.MeetingParticipant;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * An external guest attendee of a meeting, identified only by email. External
 * attendees are not held in the User service and are stored as-is (not validated
 * against it). {@code isPresent} records attendance - settable on create and
 * update.
 */
public record ExternalAttendeeDto(
        @NotBlank @Email @Size(max = 100) String email,
        @JsonProperty("isPresent") boolean isPresent
) {
    public static ExternalAttendeeDto from(MeetingParticipant p) {
        return new ExternalAttendeeDto(p.getUserId(), p.isPresent());
    }
}
