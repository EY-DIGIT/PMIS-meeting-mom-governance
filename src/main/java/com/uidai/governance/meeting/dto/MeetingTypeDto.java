package com.uidai.governance.meeting.dto;

import com.uidai.governance.meeting.domain.MeetingType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request/response DTO for configurable meeting types (MEET-FR-02.4).
 */
public record MeetingTypeDto(
        Long id,
        @NotBlank @Size(max = 50) String code,
        @NotBlank @Size(max = 150) String displayName,
        @Size(max = 500) String description,
        boolean requiresApproval,
        boolean active
) {
    public static MeetingTypeDto from(MeetingType type) {
        return new MeetingTypeDto(type.getId(), type.getCode(), type.getDisplayName(),
                type.getDescription(), type.isRequiresApproval(), type.isActive());
    }
}
