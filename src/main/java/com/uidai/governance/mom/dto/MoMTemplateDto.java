package com.uidai.governance.mom.dto;

import com.uidai.governance.mom.domain.MoMTemplate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Standardized MoM template (MEET-FR-05.1).
 */
public record MoMTemplateDto(
        Long id,
        @NotBlank @Size(max = 150) String name,
        @Size(max = 500) String description,
        @NotBlank @Size(max = 8000) String structure,
        boolean active
) {
    public static MoMTemplateDto from(MoMTemplate t) {
        return new MoMTemplateDto(t.getId(), t.getName(), t.getDescription(), t.getStructure(), t.isActive());
    }
}
