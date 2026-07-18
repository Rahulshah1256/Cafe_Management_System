package com.inn.cafe.exception;

/**
 * Thrown when an authenticated user attempts an action they do not have the role/permissions for.
 */
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
