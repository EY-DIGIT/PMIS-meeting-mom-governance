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
 * title, date, start/end time, project and at least one attendee.
 *
 * <p>A project activity is created internally for the meeting &mdash; no activity
 * block is sent by the caller, and the milestone is resolved automatically from
 * the project's {@code meetingMilestoneId} (looked up by {@link #projectId()}).</p>
 *
 * <p>{@code attendees} are internal users validated against the User service;
 * {@code externalAttendees} are optional external guests that are stored as-is
 * and are not validated. {@code attachments} are forwarded as-is to the created
 * project activity.</p>
 */
public record CreateMeetingRequest(
        @NotBlank @Size(max = 250) @Schema(example = "Quarterly governance sync") String title,
        @NotNull @Schema(type = "string", example = "2026-06-04") LocalDate meetingDate,
        @NotNull @Schema(type = "string", example = "09:00") LocalTime startTime,
        @NotNull @Schema(type = "string", example = "10:30") LocalTime endTime,
        @Size(max = 5000) @Schema(example = "Review milestones and risks") String description,
        @Size(max = 1000) @Schema(example = "https://meet.example.com/abc") String meetingLink,
        @NotBlank @Schema(description = "Project id; its meetingMilestoneId is used for the activity",
                example = "dbbff5f8-a814-4c2e-9a86-82a8f83bdea9") String projectId,
        @NotEmpty @Valid List<ParticipantDto> attendees,
        @Valid List<ExternalAttendeeDto> externalAttendees,
        @Schema(description = "Attachment references; forwarded to the activity only when non-empty")
        List<String> attachments
) {
}
