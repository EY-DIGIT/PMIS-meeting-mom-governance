package com.uidai.governance.common.exception;

/**
 * Thrown when an external Python service (User or Activity) is unavailable or
 * returns an error. Mapped to HTTP 502.
 */
public class ExternalServiceException extends RuntimeException {

    public ExternalServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public ExternalServiceException(String message) {
        super(message);
    }
}
