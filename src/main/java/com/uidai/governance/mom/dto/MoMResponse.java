package com.uidai.governance.mom.dto;

import com.uidai.governance.mom.domain.MinutesOfMeeting;
import com.uidai.governance.mom.domain.MoMStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Full MoM representation including its structured sections (MEET-FR-04.2). */
public record MoMResponse(
        Long id,
        UUID meetingId,
        Long templateId,
        String title,
        String content,
        MoMStatus status,
        List<DecisionDto> decisions,
        List<ActionItemResponse> actionItems,
        List<RiskDto> risks,
        String createdBy,
        Instant createdAt,
        String updatedBy,
        Instant updatedAt
) {
    public static MoMResponse from(MinutesOfMeeting m) {
        return new MoMResponse(
                m.getId(),
                m.getMeeting().getId(),
                m.getTemplate() != null ? m.getTemplate().getId() : null,
                m.getTitle(),
                m.getContent(),
                m.getStatus(),
                m.getDecisions().stream().map(DecisionDto::from).toList(),
                m.getActionItems().stream().map(ActionItemResponse::from).toList(),
                m.getRisks().stream().map(RiskDto::from).toList(),
                m.getCreatedBy(),
                m.getCreatedAt(),
                m.getUpdatedBy(),
                m.getUpdatedAt());
    }
}
