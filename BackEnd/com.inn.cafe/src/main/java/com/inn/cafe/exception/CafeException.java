package com.inn.cafe.exception;

/**
 * Generic application-level exception for cafe-domain failures that don't fit the more specific
 * ResourceNotFoundException / ValidationException / UnauthorizedException types.
 */
public class CafeException extends RuntimeException {
    public CafeException(String message) {
        super(message);
    }

    public CafeException(String message, Throwable cause) {
        super(message, cause);
    }
}
