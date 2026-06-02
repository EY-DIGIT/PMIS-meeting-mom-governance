package com.uidai.governance.mom.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * Request to record a MoM for a meeting (MEET-FR-04.1). Structured into
 * decisions, action items and risks (MEET-FR-04.2). May reference a standardized
 * template (MEET-FR-05.1).
 */
public record CreateMoMRequest(
        @NotBlank @Size(max = 250) String title,
        Long templateId,
        @Size(max = 20000) String content,
        @Valid List<DecisionDto> decisions,
        @Valid List<ActionItemInput> actionItems,
        @Valid List<RiskDto> risks
) {
    public List<DecisionDto> decisions() {
        return decisions != null ? decisions : List.of();
    }

    public List<ActionItemInput> actionItems() {
        return actionItems != null ? actionItems : List.of();
    }

    public List<RiskDto> risks() {
        return risks != null ? risks : List.of();
    }
}
