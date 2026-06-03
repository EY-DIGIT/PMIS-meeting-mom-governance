package com.uidai.governance.external.activity.dto;

/**
 * Standard response envelope used by the external Python project service, e.g.
 * {@code {"data": { ...activity... }, "message": null, "error": null, "status": 201}}.
 *
 * <p>Used to unwrap the {@link ActivityDto} payload returned by the activity
 * create endpoint. Unknown fields are ignored by Jackson.</p>
 */
public record ActivityEnvelope(
        ActivityDto data,
        String message,
        String error,
        Integer status
) {
}
