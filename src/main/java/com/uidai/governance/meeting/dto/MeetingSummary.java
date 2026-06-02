package com.uidai.governance.meeting.dto;

import com.uidai.governance.meeting.domain.Meeting;
import com.uidai.governance.meeting.domain.MeetingStatus;
import java.time.Instant;

/**
 * Lightweight meeting projection used in list / report responses
 * (MEET-FR-02.3).
 */
public record MeetingSummary(
        Long id,
        String title,
        String meetingTypeCode,
        Instant meetingDate,
        Long projectId,
        MeetingStatus status
) {
    public static MeetingSummary from(Meeting m) {
        return new MeetingSummary(m.getId(), m.getTitle(), m.getMeetingType().getCode(),
                m.getMeetingDate(), m.getProjectId(), m.getStatus());
    }
}
