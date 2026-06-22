package com.deliveryhub.application.usecase;

import com.deliveryhub.application.dto.LoginResponse;
import com.deliveryhub.application.port.out.PasswordEncoderPort;
import com.deliveryhub.application.port.out.UserRepositoryPort;
import com.deliveryhub.domain.enums.UserRole;
import com.deliveryhub.domain.exception.InvalidCredentialsException;
import com.deliveryhub.domain.model.User;
import com.deliveryhub.infrastructure.security.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LoginUseCaseImplTest {

    private UserRepositoryPort userRepositoryPort;
    private PasswordEncoderPort passwordEncoderPort;
    private JwtUtils jwtUtils;
    private LoginUseCaseImpl loginUseCase;

    @BeforeEach
    void setUp() {
        userRepositoryPort = mock(UserRepositoryPort.class);
        passwordEncoderPort = mock(PasswordEncoderPort.class);
        jwtUtils = mock(JwtUtils.class);
        loginUseCase = new LoginUseCaseImpl(userRepositoryPort, passwordEncoderPort, jwtUtils);
    }

    @Test
    void execute_withValidCredentials_shouldReturnLoginResponse() {
        // Arrange
        String email = "john@example.com";
        String rawPassword = "password123";
        String encodedPassword = "encodedPassword123";
        UUID userId = UUID.randomUUID();
        User user = User.reconstruct(userId, "John Doe", email, encodedPassword, UserRole.DELIVERY);

        when(userRepositoryPort.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoderPort.matches(rawPassword, encodedPassword)).thenReturn(true);
        when(jwtUtils.generateToken(email)).thenReturn("dummy-jwt-token");

        // Act
        LoginResponse response = loginUseCase.execute(email, rawPassword);

        // Assert
        assertNotNull(response);
        assertEquals("dummy-jwt-token", response.token());
        assertEquals(userId, response.userId());
        assertEquals("DELIVERY", response.role());
    }

    @Test
    void execute_withNonExistentEmail_shouldThrowInvalidCredentialsException() {
        // Arrange
        String email = "nonexistent@example.com";
        when(userRepositoryPort.findByEmail(email)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(InvalidCredentialsException.class, () -> loginUseCase.execute(email, "password"));
        verify(passwordEncoderPort, never()).matches(anyString(), anyString());
        verify(jwtUtils, never()).generateToken(anyString());
    }

    @Test
    void execute_withIncorrectPassword_shouldThrowInvalidCredentialsException() {
        // Arrange
        String email = "john@example.com";
        String rawPassword = "wrongPassword";
        String encodedPassword = "encodedPassword123";
        User user = User.reconstruct(UUID.randomUUID(), "John Doe", email, encodedPassword, UserRole.DELIVERY);

        when(userRepositoryPort.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoderPort.matches(rawPassword, encodedPassword)).thenReturn(false);

        // Act & Assert
        assertThrows(InvalidCredentialsException.class, () -> loginUseCase.execute(email, rawPassword));
        verify(jwtUtils, never()).generateToken(anyString());
    }
}
