package com.uidai.governance.external.user.dto;

import java.util.List;

/**
 * Outcome of validating a set of participants against authorized roles
 * (MEET-FR-03.2).
 *
 * @param valid          true when every requested user is authorized
 * @param invalidUserIds user ids that are unknown, inactive, or lack a
 *                       permitted role
 */
public record RoleValidationResult(
        boolean valid,
        List<String> invalidUserIds
) {
}
