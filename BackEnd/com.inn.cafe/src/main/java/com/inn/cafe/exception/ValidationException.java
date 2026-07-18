package com.inn.cafe.exception;

/**
 * Thrown for business-rule validation failures (e.g. duplicate email, invalid request payload)
 * that should surface as HTTP 400 Bad Request.
 */
public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}
