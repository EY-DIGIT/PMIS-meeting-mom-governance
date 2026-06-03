package com.uidai.governance.external.user.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.List;

/**
 * User as resolved from the external Python User service
 * ({@code GET /users/api/v3/users/{id}}). The service returns this object inside
 * a {@link UserEnvelope} {@code data} field.
 *
 * <p>Field names are mapped from the service's {@code snake_case} JSON. Unknown
 * properties are ignored so the service can evolve without breaking this client.</p>
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public record UserDto(
        String id,
        String userCode,
        String login,
        String email,
        String firstName,
        String lastName,
        String fullName,
        String status,
        String vendorId,
        String vendorName,
        String division,
        String phoneNumber,
        List<OrgRole> orgRole,
        Boolean isAdmin,
        Boolean isSuperAdmin
) {

    /** True when the user account is active and may participate in meetings. */
    public boolean active() {
        return "active".equalsIgnoreCase(status);
    }

    /** A role assignment held by the user (entry of the {@code org_role} array). */
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record OrgRole(
            String roleName,
            Integer roleId,
            String scope,
            String organizationId,
            String projectId,
            String projectCode
    ) {
    }
}
