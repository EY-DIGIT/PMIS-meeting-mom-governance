package com.uidai.governance.meeting.dto;

import com.uidai.governance.meeting.domain.Meeting;
import com.uidai.governance.meeting.domain.MeetingStatus;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Lightweight meeting projection used in list / report responses
 * (MEET-FR-02.3).
 */
public record MeetingSummary(
        Long id,
        String title,
        LocalDate meetingDate,
        LocalTime startTime,
        LocalTime endTime,
        String projectId,
        MeetingStatus status
) {
    public static MeetingSummary from(Meeting m) {
        return new MeetingSummary(m.getId(), m.getTitle(), m.getMeetingDate(),
                m.getStartTime(), m.getEndTime(), m.getProjectId(), m.getStatus());
    }
}
