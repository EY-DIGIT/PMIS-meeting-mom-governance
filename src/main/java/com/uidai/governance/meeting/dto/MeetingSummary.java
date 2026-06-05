package com.uidai.governance.meeting.dto;

import com.uidai.governance.meeting.domain.Meeting;
import com.uidai.governance.meeting.domain.MeetingStatus;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Lightweight meeting projection used in list / report responses
 * (MEET-FR-02.3).
 */
public record MeetingSummary(
        UUID id,
        String meetingCode,
        String title,
        LocalDate meetingDate,
        LocalTime startTime,
        LocalTime endTime,
        String projectId,
        String projectCode,
        String projectName,
        MeetingStatus status
) {
    public static MeetingSummary from(Meeting m) {
        return new MeetingSummary(m.getId(), m.getMeetingCode(), m.getTitle(), m.getMeetingDate(),
                m.getStartTime(), m.getEndTime(), m.getProjectId(),
                m.getProjectCode(), m.getProjectName(), m.getStatus());
    }
}
