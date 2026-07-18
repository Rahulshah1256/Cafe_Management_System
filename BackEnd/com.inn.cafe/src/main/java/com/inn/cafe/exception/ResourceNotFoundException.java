package com.inn.cafe.exception;

/**
 * Thrown when a requested entity (category, product, bill, user, ...) cannot be found by id.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
