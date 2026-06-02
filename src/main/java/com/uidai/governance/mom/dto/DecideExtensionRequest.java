package com.uidai.governance.mom.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Decision on a timeline extension request by the designated authority
 * (MEET-FR-04.5). When approved, the action item's due date is advanced to the
 * requested date.
 */
public record DecideExtensionRequest(
        boolean approve,
        @NotBlank @Size(max = 100) String decidedByUserId,
        @Size(max = 2000) String decisionComment
) {
}
