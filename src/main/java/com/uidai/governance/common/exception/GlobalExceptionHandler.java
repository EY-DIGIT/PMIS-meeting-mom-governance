package com.uidai.governance.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Translates exceptions into the standard {@link ErrorResponse} payload.
 *
 * <p>Every handled exception is logged: expected client errors (not found,
 * validation, business rule) at {@code WARN} without a stack trace, and
 * server-side / downstream failures at {@code ERROR} with the full stack trace.</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex, HttpServletRequest req) {
        log.warn("Resource not found [{} {}] at {}: {}",
                req.getMethod(), req.getRequestURI(), origin(ex), ex.getMessage());
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), req);
    }

    @ExceptionHandler(BusinessValidationException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessValidationException ex, HttpServletRequest req) {
        log.warn("Business validation failed [{} {}] at {}: {}",
                req.getMethod(), req.getRequestURI(), origin(ex), ex.getMessage());
        return build(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), req);
    }

    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<ErrorResponse> handleExternal(ExternalServiceException ex, HttpServletRequest req) {
        log.error("External service error [{} {}]: {}", req.getMethod(), req.getRequestURI(), ex.getMessage(), ex);
        return build(HttpStatus.BAD_GATEWAY, ex.getMessage(), req);
    }

    @ExceptionHandler(UpstreamAuthException.class)
    public ResponseEntity<ErrorResponse> handleUpstreamAuth(UpstreamAuthException ex, HttpServletRequest req) {
        log.warn("Upstream authentication failed [{} {}] at {}: {}",
                req.getMethod(), req.getRequestURI(), origin(ex), ex.getMessage());
        return build(HttpStatus.UNAUTHORIZED, ex.getMessage(), req);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResponse> handleMissingHeader(MissingRequestHeaderException ex, HttpServletRequest req) {
        log.warn("Missing required header [{} {}]: {}", req.getMethod(), req.getRequestURI(), ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), req);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadable(HttpMessageNotReadableException ex, HttpServletRequest req) {
        String detail = ex.getMostSpecificCause().getMessage();
        log.warn("Malformed request body [{} {}]: {}", req.getMethod(), req.getRequestURI(), detail);
        return build(HttpStatus.BAD_REQUEST, "Malformed request body: " + detail, req);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        List<ErrorResponse.FieldError> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> new ErrorResponse.FieldError(fe.getField(), fe.getDefaultMessage()))
                .toList();
        log.warn("Request validation failed [{} {}]: {}", req.getMethod(), req.getRequestURI(), fieldErrors);
        ErrorResponse body = new ErrorResponse(Instant.now(), HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(), "Validation failed", req.getRequestURI(), fieldErrors);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResource(NoResourceFoundException ex, HttpServletRequest req) {
        // Routine static-resource miss (favicon, swagger assets, scanners) - 404, not a server error.
        log.debug("No static resource [{} {}]", req.getMethod(), req.getRequestURI());
        return build(HttpStatus.NOT_FOUND, "No static resource " + req.getRequestURI(), req);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest req) {
        log.error("Unhandled exception [{} {}]: {}", req.getMethod(), req.getRequestURI(), ex.getMessage(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), req);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message, HttpServletRequest req) {
        return ResponseEntity.status(status)
                .body(ErrorResponse.of(status.value(), status.getReasonPhrase(), message, req.getRequestURI()));
    }

    /**
     * Returns the location where {@code ex} was thrown as
     * {@code Class.method(File.java:line)} - the first application frame
     * ({@code com.uidai.governance...}), falling back to the top stack frame.
     * Lets WARN-level handlers (which don't print a stack trace) still point at
     * the file and line that raised the exception.
     */
    private static String origin(Throwable ex) {
        StackTraceElement[] stack = ex.getStackTrace();
        if (stack.length == 0) {
            return "unknown";
        }
        StackTraceElement frame = stack[0];
        for (StackTraceElement el : stack) {
            if (el.getClassName().startsWith("com.uidai.governance")) {
                frame = el;
                break;
            }
        }
        String simpleClass = frame.getClassName().substring(frame.getClassName().lastIndexOf('.') + 1);
        return "%s.%s(%s:%d)".formatted(simpleClass, frame.getMethodName(),
                frame.getFileName(), frame.getLineNumber());
    }
}
