package com.deliveryhub.domain.exception;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DomainExceptionsTest {
    @Test
    void shouldBeAbleToThrowInvalidStatusTransitionException() {
        String message = "Invalid transition";
        InvalidStatusTransitionException exception = new InvalidStatusTransitionException(message);
        assertEquals(message, exception.getMessage());
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void shouldBeAbleToThrowUnauthorizedTransitionException() {
        String message = "Unauthorized";
        UnauthorizedTransitionException exception = new UnauthorizedTransitionException(message);
        assertEquals(message, exception.getMessage());
        assertTrue(exception instanceof RuntimeException);
    }
}
