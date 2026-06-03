package com.uidai.governance.external.user;

import com.uidai.governance.external.user.dto.RoleValidationResult;
import com.uidai.governance.external.user.dto.UserDto;
import java.util.List;
import java.util.Optional;

/**
 * Abstraction over the external (Python) User service.
 *
 * <p>Used to validate meeting participants against authorized roles
 * (MEET-FR-03.2) and to resolve user details for display and notification.</p>
 */
public interface UserServiceClient {

    /**
     * Validates that each of the given users exists, is active, and holds a role
     * permitted to participate in the given meeting type.
     */
    RoleValidationResult validateParticipants(List<String> userIds, String meetingType);

    /**
     * Resolves a single user's details.
     */
    Optional<UserDto> findUser(String userId);

    /**
     * Resolves multiple users in one round-trip.
     */
    List<UserDto> findUsers(List<String> userIds);

    /**
     * Lists users with pagination
     * ({@code GET /users?offset=&pageSize=&include_deleted=}).
     *
     * @param offset         1-based page offset as used by the Users service
     * @param pageSize       page size
     * @param includeDeleted whether soft-deleted users are included
     */
    List<UserDto> listUsers(int offset, int pageSize, boolean includeDeleted);
}
