package com.uidai.governance.mom.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request to update a MoM's header fields (title, template, content). Only
 * permitted while the MoM is not FINALIZED (MEET-FR-05.3). Action items,
 * decisions and risks are managed through their own endpoints.
 */
public record UpdateMoMRequest(
        @NotBlank @Size(max = 250) String title,
        Long templateId,
        @Size(max = 20000) String content
) {
}
