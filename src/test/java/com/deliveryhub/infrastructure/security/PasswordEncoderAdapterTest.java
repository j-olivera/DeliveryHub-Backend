package com.deliveryhub.infrastructure.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

class PasswordEncoderAdapterTest {

    private PasswordEncoderAdapter passwordEncoderAdapter;

    @BeforeEach
    void setUp() {
        passwordEncoderAdapter = new PasswordEncoderAdapter(new BCryptPasswordEncoder());
    }

    @Test
    void encode_shouldReturnHashedPassword() {
        // Arrange
        String rawPassword = "mySecurePassword123";

        // Act
        String encodedPassword = passwordEncoderAdapter.encode(rawPassword);

        // Assert
        assertNotNull(encodedPassword);
        assertNotEquals(rawPassword, encodedPassword);
        assertTrue(encodedPassword.startsWith("$2a$") || encodedPassword.startsWith("$2b$"));
    }

    @Test
    void matches_whenPasswordMatches_shouldReturnTrue() {
        // Arrange
        String rawPassword = "mySecurePassword123";
        String encodedPassword = passwordEncoderAdapter.encode(rawPassword);

        // Act
        boolean result = passwordEncoderAdapter.matches(rawPassword, encodedPassword);

        // Assert
        assertTrue(result);
    }

    @Test
    void matches_whenPasswordDoesNotMatch_shouldReturnFalse() {
        // Arrange
        String rawPassword = "mySecurePassword123";
        String wrongPassword = "wrongPassword123";
        String encodedPassword = passwordEncoderAdapter.encode(rawPassword);

        // Act
        boolean result = passwordEncoderAdapter.matches(wrongPassword, encodedPassword);

        // Assert
        assertFalse(result);
    }
}
