package com.deliveryhub.domain.exception;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DomainValidationExceptionTest {

    @Test
    void shouldStoreMessage() {
        String message = "Validation failed";
        DomainValidationException exception = new DomainValidationException(message);
        assertEquals(message, exception.getMessage());
    }
}
