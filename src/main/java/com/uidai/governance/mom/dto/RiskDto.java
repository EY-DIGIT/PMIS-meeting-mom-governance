package com.uidai.governance.mom.dto;

import com.uidai.governance.mom.domain.Risk;
import com.uidai.governance.mom.domain.RiskSeverity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** A risk within a MoM (MEET-FR-04.2). */
public record RiskDto(
        Long id,
        @NotBlank @Size(max = 4000) String description,
        @NotNull RiskSeverity severity,
        @Size(max = 4000) String mitigation,
        @Size(max = 100) String ownerUserId
) {
    public static RiskDto from(Risk r) {
        return new RiskDto(r.getId(), r.getDescription(), r.getSeverity(), r.getMitigation(), r.getOwnerUserId());
    }
}
