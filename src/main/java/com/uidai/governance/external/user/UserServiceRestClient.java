package com.uidai.governance.external.user;

import com.uidai.governance.common.exception.ExternalServiceException;
import com.uidai.governance.common.exception.UpstreamAuthException;
import com.uidai.governance.common.logging.JsonLogFormatter;
import com.uidai.governance.config.ExternalApiProperties;
import com.uidai.governance.config.RestClientConfig;
import com.uidai.governance.external.user.dto.RoleValidationResult;
import com.uidai.governance.external.user.dto.UserDto;
import com.uidai.governance.external.user.dto.UserEnvelope;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * REST implementation of {@link UserServiceClient} backed by the external Python
 * User service ({@code GET /users/api/v3/users/{id}}).
 *
 * <p>Participants are validated by resolving each user and checking that the
 * account exists and is {@code active} (MEET-FR-03.2). Authentication is handled
 * transparently: the caller's bearer token is propagated from the inbound request
 * {@code Authorization} header by {@link RestClientConfig}, so no token needs to
 * be threaded through these methods.</p>
 *
 * <p>When {@code external.user-service.enabled=false} (e.g. local dev / tests),
 * the client returns permissive defaults so the module can run standalone.</p>
 */
@Component
public class UserServiceRestClient implements UserServiceClient {

    private static final Logger log = LoggerFactory.getLogger(UserServiceRestClient.class);

    private static final String EP_GET_USER = "get-user";
    private static final String EP_LIST_USERS = "list-users";

    private final RestClient restClient;
    private final ExternalApiProperties.ServiceConfig config;
    private final JsonLogFormatter jsonLog;
    private final boolean enabled;

    public UserServiceRestClient(@Qualifier(RestClientConfig.USER_CLIENT) RestClient restClient,
                                 ExternalApiProperties properties,
                                 JsonLogFormatter jsonLog) {
        this.restClient = restClient;
        this.config = properties.userService();
        this.jsonLog = jsonLog;
        this.enabled = config.enabled();
    }

    @Override
    public RoleValidationResult validateParticipants(List<String> userIds, String meetingType) {
        if (!enabled) {
            log.debug("User service disabled - treating {} participants as valid", userIds.size());
            return new RoleValidationResult(true, List.of());
        }
        List<String> invalid = new ArrayList<>();
        for (String userId : userIds) {
            UserDto user = findUser(userId).orElse(null);
            if (user == null || !user.active()) {
                invalid.add(userId);
            }
        }
        return new RoleValidationResult(invalid.isEmpty(), invalid);
    }

    @Override
    public Optional<UserDto> findUser(String userId) {
        if (!enabled) {
            return Optional.empty();
        }
        String path = config.endpoint(EP_GET_USER);
        log.info("Calling User service: GET {}{} request={}",
                config.baseUrl(), path, jsonLog.toJson(Map.of("userId", userId)));
        try {
            UserEnvelope envelope = restClient.get()
                    .uri(path, userId)
                    .retrieve()
                    .onStatus(status -> status.value() == 401 || status.value() == 403, (req, res) -> {
                        throw new UpstreamAuthException("User service rejected the bearer token (HTTP "
                                + res.getStatusCode().value()
                                + "): the token is missing, expired or invalid.");
                    })
                    .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                        // Unknown user (e.g. 404) -> treated as "not found" (empty) below.
                    })
                    .body(UserEnvelope.class);
            return Optional.ofNullable(envelope).map(UserEnvelope::data);
        } catch (RestClientException ex) {
            log.error("User service call failed: GET {}{} request={}",
                    config.baseUrl(), path, jsonLog.toJson(Map.of("userId", userId)), ex);
            throw new ExternalServiceException("User service lookup failed for " + userId, ex);
        }
    }

    @Override
    public List<UserDto> findUsers(List<String> userIds) {
        if (!enabled || userIds.isEmpty()) {
            return List.of();
        }
        List<UserDto> users = new ArrayList<>(userIds.size());
        for (String userId : userIds) {
            findUser(userId).ifPresent(users::add);
        }
        return users;
    }

    @Override
    public List<UserDto> listUsers(int offset, int pageSize, boolean includeDeleted) {
        if (!enabled) {
            return List.of();
        }
        String path = config.endpoint(EP_LIST_USERS);
        log.info("Calling User service: GET {}{} request={}", config.baseUrl(), path,
                jsonLog.toJson(Map.of("offset", offset, "pageSize", pageSize, "includeDeleted", includeDeleted)));
        try {
            // GET /users?offset=&pageSize=&include_deleted=
            // TODO: confirm the list response envelope shape with the User service
            // team; this currently maps a bare array and is not yet exercised.
            List<UserDto> users = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path(path)
                            .queryParam("offset", offset)
                            .queryParam("pageSize", pageSize)
                            .queryParam("include_deleted", includeDeleted)
                            .build())
                    .retrieve()
                    .body(new org.springframework.core.ParameterizedTypeReference<List<UserDto>>() {
                    });
            return users != null ? users : List.of();
        } catch (RestClientException ex) {
            log.error("User service call failed: GET {}{} request={}", config.baseUrl(), path,
                    jsonLog.toJson(Map.of("offset", offset, "pageSize", pageSize, "includeDeleted", includeDeleted)),
                    ex);
            throw new ExternalServiceException("User service list failed", ex);
        }
    }
}
