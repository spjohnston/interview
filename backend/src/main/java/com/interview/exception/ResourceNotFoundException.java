package com.interview.exception;

/**
 * Thrown when a requested resource cannot be found. Translated to HTTP 404
 * by {@link GlobalExceptionHandler}.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
