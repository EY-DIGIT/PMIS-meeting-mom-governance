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
 * Request to create a meeting. Mandatory fields are enforced via validation:
 * title, date, start/end time, project, milestone and at least one attendee.
 *
 * <p>A project activity is created internally for the meeting (under
 * {@link #milestoneId()}) from these fields &mdash; no activity block is sent by
 * the caller.</p>
 *
 * <p>{@code attendees} are internal users validated against the User service;
 * {@code externalAttendees} are optional external guests that are stored as-is
 * and are not validated. {@code attachments} are forwarded as-is to the created
 * project activity.</p>
 */
public record CreateMeetingRequest(
        @NotBlank @Size(max = 250) String title,
        @NotNull @Schema(type = "string", example = "2026-06-04") LocalDate meetingDate,
        @NotNull @Schema(type = "string", example = "09:00") LocalTime startTime,
        @NotNull @Schema(type = "string", example = "10:30") LocalTime endTime,
        @Size(max = 5000) String description,
        @Size(max = 1000) String meetingLink,
        @NotBlank String projectId,
        @NotBlank String milestoneId,
        @NotEmpty @Valid List<ParticipantDto> attendees,
        @Valid List<ExternalAttendeeDto> externalAttendees,
        List<String> attachments
) {
}
