package com.uidai.governance.meeting.dto;

import com.uidai.governance.meeting.domain.Meeting;
import com.uidai.governance.meeting.domain.MeetingStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Full meeting representation returned by the API.
 */
public record MeetingResponse(
        Long id,
        String title,
        LocalDate meetingDate,
        LocalTime startTime,
        LocalTime endTime,
        String description,
        String meetingLink,
        String projectId,
        MeetingStatus status,
        String activityId,
        String activityProjectId,
        String activityMilestoneId,
        String activityName,
        String activityDescription,
        List<ParticipantDto> attendees,
        String createdBy,
        Instant createdAt,
        String updatedBy,
        Instant updatedAt
) {
    public static MeetingResponse from(Meeting m) {
        return new MeetingResponse(
                m.getId(),
                m.getTitle(),
                m.getMeetingDate(),
                m.getStartTime(),
                m.getEndTime(),
                m.getDescription(),
                m.getMeetingLink(),
                m.getProjectId(),
                m.getStatus(),
                m.getActivityId(),
                m.getActivityProjectId(),
                m.getActivityMilestoneId(),
                m.getActivityName(),
                m.getActivityDescription(),
                m.getParticipants().stream().map(ParticipantDto::from).toList(),
                m.getCreatedBy(),
                m.getCreatedAt(),
                m.getUpdatedBy(),
                m.getUpdatedAt());
    }
}
