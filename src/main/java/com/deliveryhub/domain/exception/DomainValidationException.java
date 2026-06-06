package com.deliveryhub.domain.exception;

/**
 * Exception thrown when a domain validation rule is violated.
 */
public class DomainValidationException extends RuntimeException {
    public DomainValidationException(String message) {
        super(message);
    }
}
