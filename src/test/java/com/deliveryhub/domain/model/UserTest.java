package com.deliveryhub.domain.model;

import com.deliveryhub.domain.enums.UserRole;
import com.deliveryhub.domain.exception.DomainValidationException;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void shouldCreateUserWithValidData() {
        String name = "John Doe";
        String email = "john@example.com";
        String password = "hashed_password";
        UserRole role = UserRole.DELIVERY;

        User user = User.create(name, email, password, role);

        assertNotNull(user.getId());
        assertEquals(name, user.getName());
        assertEquals(email, user.getEmail());
        assertEquals(password, user.getPassword());
        assertEquals(role, user.getRole());
    }

    @Test
    void shouldThrowExceptionWhenNameIsBlank() {
        assertThrows(DomainValidationException.class, () ->
            User.create("", "john@example.com", "password", UserRole.ADMIN)
        );
    }

    @Test
    void shouldThrowExceptionWhenEmailIsBlank() {
        assertThrows(DomainValidationException.class, () ->
            User.create("John Doe", " ", "password", UserRole.ADMIN)
        );
    }

    @Test
    void shouldThrowExceptionWhenRoleIsNull() {
        assertThrows(DomainValidationException.class, () ->
            User.create("John Doe", "john@example.com", "password", null)
        );
    }

    @Test
    void shouldThrowExceptionWhenEmailFormatIsInvalid() {
        assertThrows(DomainValidationException.class, () ->
            User.create("John Doe", "invalid-email", "password", UserRole.ADMIN)
        );
    }

    @Test
    void shouldReconstructUser() {
        UUID id = UUID.randomUUID();
        String name = "John Doe";
        String email = "john@example.com";
        String password = "hashed_password";
        UserRole role = UserRole.DELIVERY;

        User user = User.reconstruct(id, name, email, password, role);

        assertEquals(id, user.getId());
        assertEquals(name, user.getName());
        assertEquals(email, user.getEmail());
        assertEquals(password, user.getPassword());
        assertEquals(role, user.getRole());
    }
}
