package com.uidai.governance.common.exception;

/**
 * Raised when an external service rejects the caller's bearer token
 * (HTTP 401/403). Surfaced to the client as 401 Unauthorized so an expired or
 * invalid token is reported as an authentication problem rather than being
 * masked as a business-validation failure (e.g. "participant not authorized").
 */
public class UpstreamAuthException extends RuntimeException {

    public UpstreamAuthException(String message) {
        super(message);
    }
}
