package com.uidai.governance.common.exception;

/**
 * Carries an error response received from an external service (its HTTP status
 * and raw response body) so it can be surfaced to the caller as-is, instead of
 * being masked behind a generic gateway/validation error.
 */
public class ExternalApiException extends RuntimeException {

    private final int status;
    private final String body;

    public ExternalApiException(int status, String body) {
        super("External API responded with HTTP " + status + (body == null ? "" : ": " + body));
        this.status = status;
        this.body = body;
    }

    /** HTTP status returned by the external service. */
    public int status() {
        return status;
    }

    /** Raw response body returned by the external service (typically JSON). */
    public String body() {
        return body;
    }
}
