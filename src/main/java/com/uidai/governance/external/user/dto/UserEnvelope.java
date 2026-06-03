package com.uidai.governance.external.user.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Standard response envelope used by the external Python User service, e.g.
 * {@code {"data": { ...user... }, "message": null, "error": null, "status": 200}}.
 *
 * <p>Used to unwrap the {@link UserDto} payload returned by the user lookup
 * endpoint. Unknown fields are ignored by Jackson.</p>
 *
 * <p>On error responses (e.g. {@code 404 USER_NOT_FOUND}, {@code 401
 * AUTH_REQUIRED}) the service sets {@code data} to {@code null} and {@code error}
 * to an error <em>object</em>; it is typed as {@link Object} here because callers
 * only need {@code data} to determine whether the user was resolved.</p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record UserEnvelope(
        UserDto data,
        String message,
        Object error,
        Integer status
) {
}
