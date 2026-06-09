package com.uidai.governance.meeting.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.uidai.governance.meeting.domain.Meeting;
import com.uidai.governance.meeting.domain.MeetingParticipant;
import com.uidai.governance.meeting.domain.MeetingStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

/**
 * Full meeting representation returned by the API.
 */
public record MeetingResponse(
        UUID id,
        String meetingCode,
        String title,
        LocalDate meetingDate,
        LocalTime startTime,
        LocalTime endTime,
        String description,
        String meetingLink,
        String projectId,
        String projectCode,
        String projectName,
        MeetingStatus status,
        String activityId,
        String activityProjectId,
        String activityMilestoneId,
        String activityName,
        String activityDescription,
        JsonNode attachments,
        List<ParticipantDto> attendees,
        List<ExternalAttendeeDto> externalAttendees,
        String createdBy,
        Instant createdAt,
        String updatedBy,
        Instant updatedAt
) {
    public static MeetingResponse from(Meeting m) {
        List<ParticipantDto> attendees = m.getParticipants().stream()
                .filter(p -> !p.isExternal()).map(ParticipantDto::from).toList();
        List<ExternalAttendeeDto> externalAttendees = m.getParticipants().stream()
                .filter(MeetingParticipant::isExternal).map(ExternalAttendeeDto::from).toList();
        return new MeetingResponse(
                m.getId(),
                m.getMeetingCode(),
                m.getTitle(),
                m.getMeetingDate(),
                m.getStartTime(),
                m.getEndTime(),
                m.getDescription(),
                m.getMeetingLink(),
                m.getProjectId(),
                m.getProjectCode(),
                m.getProjectName(),
                m.getStatus(),
                m.getActivityId(),
                m.getActivityProjectId(),
                m.getActivityMilestoneId(),
                m.getActivityName(),
                m.getActivityDescription(),
                m.getAttachments(),
                attendees,
                externalAttendees,
                m.getCreatedBy(),
                m.getCreatedAt(),
                m.getUpdatedBy(),
                m.getUpdatedAt());
    }
}
