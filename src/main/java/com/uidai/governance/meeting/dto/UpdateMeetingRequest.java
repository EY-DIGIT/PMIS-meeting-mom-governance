package com.uidai.governance.meeting.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Request to update an existing meeting's mutable fields. Updating a meeting does
 * not re-create its linked project activity, so no milestone is required here.
 *
 * <p>Attendance is captured per attendee via {@code isPresent} on each entry of
 * {@code attendees} / {@code externalAttendees}.</p>
 */
public record UpdateMeetingRequest(
        @NotBlank @Size(max = 250) @Schema(example = "Quarterly governance sync") String title,
        @NotNull @Schema(type = "string", example = "2026-06-04") LocalDate meetingDate,
        @NotNull @Schema(type = "string", example = "09:00") LocalTime startTime,
        @NotNull @Schema(type = "string", example = "10:30") LocalTime endTime,
        @Size(max = 5000) @Schema(example = "Review milestones and risks") String description,
        @Size(max = 1000) @Schema(example = "https://meet.example.com/abc") String meetingLink,
        @NotBlank @Schema(example = "dbbff5f8-a814-4c2e-9a86-82a8f83bdea9") String projectId,
        @NotEmpty @Valid List<ParticipantDto> attendees,
        @Valid List<ExternalAttendeeDto> externalAttendees
) {
}
