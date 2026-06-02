package com.uidai.governance.external.user.dto;

import java.util.List;

/**
 * User as resolved from the external Python User service.
 *
 * <p>TODO: align field names with the Python API signature once provided.</p>
 */
public record UserDto(
        String userId,
        String displayName,
        String email,
        List<String> roles,
        boolean active
) {
}
