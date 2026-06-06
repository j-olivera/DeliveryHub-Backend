package com.deliveryhub.domain.exception;

public class UnauthorizedTransitionException extends RuntimeException {
    public UnauthorizedTransitionException(String message) {
        super(message);
    }
}
