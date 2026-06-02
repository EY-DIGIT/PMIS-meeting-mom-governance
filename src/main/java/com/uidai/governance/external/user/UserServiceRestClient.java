package com.uidai.governance.external.user;

import com.uidai.governance.common.exception.ExternalServiceException;
import com.uidai.governance.config.ExternalApiProperties;
import com.uidai.governance.config.RestClientConfig;
import com.uidai.governance.external.user.dto.RoleValidationResult;
import com.uidai.governance.external.user.dto.UserDto;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * REST implementation of {@link UserServiceClient} backed by the external Python
 * User service.
 *
 * <p><b>Awaiting API signature:</b> the request paths, query parameters and the
 * request/response body shapes below are placeholders. Once the Python API
 * signature is provided, update the {@code uri(...)} paths and the body mapping
 * (and the DTOs in {@code dto/}) to match. The surrounding error handling,
 * timeouts and the disabled-mode fallback do not need to change.</p>
 *
 * <p>When {@code external.user-service.enabled=false} (e.g. local dev / tests),
 * the client returns permissive defaults so the module can run standalone.</p>
 */
@Component
public class UserServiceRestClient implements UserServiceClient {

    private static final Logger log = LoggerFactory.getLogger(UserServiceRestClient.class);

    private final RestClient restClient;
    private final boolean enabled;

    public UserServiceRestClient(@Qualifier(RestClientConfig.USER_CLIENT) RestClient restClient,
                                 ExternalApiProperties properties) {
        this.restClient = restClient;
        this.enabled = properties.userService().enabled();
    }

    @Override
    public RoleValidationResult validateParticipants(List<String> userIds, String meetingType) {
        if (!enabled) {
            log.debug("User service disabled - treating {} participants as valid", userIds.size());
            return new RoleValidationResult(true, List.of());
        }
        try {
            // TODO: replace path/body with the Python User service signature.
            // Assumed contract: POST /users/validate-roles
            //   request : { "userIds": [...], "context": "<meetingType>" }
            //   response: { "valid": bool, "invalidUserIds": [...] }
            RoleValidationResult result = restClient.post()
                    .uri("/users/validate-roles")
                    .body(new ValidateRequest(userIds, meetingType))
                    .retrieve()
                    .body(RoleValidationResult.class);
            return result != null ? result : new RoleValidationResult(false, userIds);
        } catch (RestClientException ex) {
            throw new ExternalServiceException("User service role validation failed", ex);
        }
    }

    @Override
    public Optional<UserDto> findUser(String userId) {
        if (!enabled) {
            return Optional.empty();
        }
        try {
            // TODO: replace with the Python User service signature. Assumed: GET /users/{id}
            return Optional.ofNullable(restClient.get()
                    .uri("/users/{id}", userId)
                    .retrieve()
                    .body(UserDto.class));
        } catch (RestClientException ex) {
            throw new ExternalServiceException("User service lookup failed for " + userId, ex);
        }
    }

    @Override
    public List<UserDto> findUsers(List<String> userIds) {
        if (!enabled || userIds.isEmpty()) {
            return List.of();
        }
        try {
            // TODO: replace with the Python User service signature. Assumed: POST /users/batch
            List<UserDto> users = restClient.post()
                    .uri("/users/batch")
                    .body(userIds)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<UserDto>>() {
                    });
            return users != null ? users : List.of();
        } catch (RestClientException ex) {
            throw new ExternalServiceException("User service batch lookup failed", ex);
        }
    }

    @Override
    public List<UserDto> listUsers(int offset, int pageSize, boolean includeDeleted) {
        if (!enabled) {
            return List.of();
        }
        try {
            // GET /users?offset=&pageSize=&include_deleted=
            // TODO: confirm the response envelope. This currently expects a bare
            // JSON array of users; if the service wraps it (e.g. {"data":[...]},
            // {"users":[...], "total":N}) introduce a wrapper record and map it.
            List<UserDto> users = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/users")
                            .queryParam("offset", offset)
                            .queryParam("pageSize", pageSize)
                            .queryParam("include_deleted", includeDeleted)
                            .build())
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<UserDto>>() {
                    });
            return users != null ? users : List.of();
        } catch (RestClientException ex) {
            throw new ExternalServiceException("User service list failed", ex);
        }
    }

    /** Placeholder request body for participant validation. */
    private record ValidateRequest(List<String> userIds, String context) {
    }
}
