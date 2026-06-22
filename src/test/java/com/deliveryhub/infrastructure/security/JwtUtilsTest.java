package com.deliveryhub.infrastructure.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilsTest {

    private JwtUtils jwtUtils;
    private final String secretString = "mySuperSecretKeyForJwtGenerationMustBeAtLeast256BitsLong!";
    private final int expirationMs = 60000; // 1 minuto

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils(secretString, expirationMs);
    }

    @Test
    void generateToken_shouldReturnValidToken() {
        // Arrange
        String username = "testuser";

        // Act
        String token = jwtUtils.generateToken(username);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());

        // Verificar que podemos validar y leer el claim
        assertTrue(jwtUtils.validateToken(token));
        assertEquals(username, jwtUtils.getUsernameFromToken(token));
    }

    @Test
    void validateToken_whenTokenIsExpired_shouldReturnFalse() throws InterruptedException {
        // Arrange
        JwtUtils shortLivedJwtUtils = new JwtUtils(secretString, 1); // 1 ms de expiración
        String token = shortLivedJwtUtils.generateToken("expiredUser");

        // Act
        Thread.sleep(5); // Esperar a que expire
        boolean isValid = shortLivedJwtUtils.validateToken(token);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void validateToken_whenTokenIsInvalid_shouldReturnFalse() {
        // Arrange
        String invalidToken = "invalid.token.here";

        // Act
        boolean isValid = jwtUtils.validateToken(invalidToken);

        // Assert
        assertFalse(isValid);
    }
}
