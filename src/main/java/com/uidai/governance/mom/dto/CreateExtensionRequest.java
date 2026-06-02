package com.uidai.governance.mom.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/**
 * Request to extend an action item's due date (MEET-FR-04.5). Justification is
 * mandatory and documented.
 */
public record CreateExtensionRequest(
        @NotNull LocalDate requestedDueDate,
        @NotBlank @Size(max = 4000) String justification,
        @NotBlank @Size(max = 100) String requestedByUserId
) {
}
