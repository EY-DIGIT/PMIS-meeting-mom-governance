package com.uidai.governance.mom.dto;

import com.uidai.governance.mom.domain.Decision;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** A decision within a MoM (MEET-FR-04.2). */
public record DecisionDto(
        Long id,
        @NotBlank @Size(max = 4000) String description,
        @Size(max = 100) String ownerUserId
) {
    public static DecisionDto from(Decision d) {
        return new DecisionDto(d.getId(), d.getDescription(), d.getOwnerUserId());
    }
}
