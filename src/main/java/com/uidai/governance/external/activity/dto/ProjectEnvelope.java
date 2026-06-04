package com.uidai.governance.external.activity.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Standard response envelope wrapping a {@link ProjectDto} returned by the
 * external Python project service ({@code {"data": {...}, "message": null,
 * "error": null, "status": 200}}). {@code error} is typed as {@link Object}
 * because the service returns an error object on failures. Unknown fields ignored.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ProjectEnvelope(
        ProjectDto data,
        String message,
        Object error,
        Integer status
) {
}
