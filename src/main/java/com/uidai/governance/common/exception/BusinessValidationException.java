package com.uidai.governance.common.exception;

/**
 * Thrown when a business rule is violated (e.g. invalid participant role,
 * illegal workflow transition, missing justification). Mapped to HTTP 422.
 */
public class BusinessValidationException extends RuntimeException {

    public BusinessValidationException(String message) {
        super(message);
    }
}
