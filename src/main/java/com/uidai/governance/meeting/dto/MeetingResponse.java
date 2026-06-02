package com.uidai.governance.meeting.dto;

import com.uidai.governance.meeting.domain.Meeting;
import com.uidai.governance.meeting.domain.MeetingStatus;
import java.time.Instant;
import java.util.List;

/**
 * Full meeting representation returned by the API.
 */
public record MeetingResponse(
        Long id,
        String title,
        String meetingTypeCode,
        String meetingTypeName,
        Instant meetingDate,
        Long projectId,
        Long stageId,
        Long serviceProviderId,
        String agenda,
        MeetingStatus status,
        List<ParticipantDto> participants,
        String createdBy,
        Instant createdAt,
        String updatedBy,
        Instant updatedAt
) {
    public static MeetingResponse from(Meeting m) {
        return new MeetingResponse(
                m.getId(),
                m.getTitle(),
                m.getMeetingType().getCode(),
                m.getMeetingType().getDisplayName(),
                m.getMeetingDate(),
                m.getProjectId(),
                m.getStageId(),
                m.getServiceProviderId(),
                m.getAgenda(),
                m.getStatus(),
                m.getParticipants().stream().map(ParticipantDto::from).toList(),
                m.getCreatedBy(),
                m.getCreatedAt(),
                m.getUpdatedBy(),
                m.getUpdatedAt());
    }
}
