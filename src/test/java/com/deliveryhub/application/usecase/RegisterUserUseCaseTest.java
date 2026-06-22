package com.deliveryhub.application.usecase;

import com.deliveryhub.application.dto.RegisterUserRequest;
import com.deliveryhub.application.port.out.PasswordEncoderPort;
import com.deliveryhub.application.port.out.UserRepositoryPort;
import com.deliveryhub.domain.enums.UserRole;
import com.deliveryhub.domain.exception.DomainValidationException;
import com.deliveryhub.domain.exception.UserAlreadyExistsException;
import com.deliveryhub.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RegisterUserUseCaseTest {

    private UserRepositoryPort userRepositoryPort;
    private PasswordEncoderPort passwordEncoderPort;
    private RegisterUserUseCaseImpl registerUserUseCase;

    @BeforeEach
    void setUp() {
        userRepositoryPort = mock(UserRepositoryPort.class);
        passwordEncoderPort = mock(PasswordEncoderPort.class);
        registerUserUseCase = new RegisterUserUseCaseImpl(userRepositoryPort, passwordEncoderPort);
    }

    @Test
    void execute_withValidRequest_shouldEncodePasswordAndSaveAndReturnUser() {
        // Arrange
        RegisterUserRequest request = new RegisterUserRequest(
            "Alice",
            "alice@example.com",
            "pwd123",
            "DELIVERY"
        );

        when(passwordEncoderPort.encode("pwd123")).thenReturn("encodedPwd123");
        when(userRepositoryPort.existsByEmail(request.email())).thenReturn(false);
        when(userRepositoryPort.existsByName(request.name())).thenReturn(false);
        when(userRepositoryPort.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User result = registerUserUseCase.execute(request);

        // Assert
        assertNotNull(result);
        assertEquals("Alice", result.getName());
        assertEquals("alice@example.com", result.getEmail());
        assertEquals("encodedPwd123", result.getPassword());
        assertEquals(UserRole.DELIVERY, result.getRole());
        verify(passwordEncoderPort).encode("pwd123");
        verify(userRepositoryPort).save(any(User.class));
    }

    @Test
    void execute_withDuplicateEmail_shouldThrowUserAlreadyExistsException() {
        // Arrange
        RegisterUserRequest request = new RegisterUserRequest(
            "Alice",
            "alice@example.com",
            "pwd123",
            "DELIVERY"
        );

        when(passwordEncoderPort.encode("pwd123")).thenReturn("encodedPwd123");
        when(userRepositoryPort.existsByEmail(request.email())).thenReturn(true);

        // Act & Assert
        assertThrows(UserAlreadyExistsException.class, () -> registerUserUseCase.execute(request));
        verify(userRepositoryPort, never()).save(any(User.class));
    }

    @Test
    void execute_withDuplicateName_shouldThrowUserAlreadyExistsException() {
        // Arrange
        RegisterUserRequest request = new RegisterUserRequest(
            "Alice",
            "alice@example.com",
            "pwd123",
            "DELIVERY"
        );

        when(passwordEncoderPort.encode("pwd123")).thenReturn("encodedPwd123");
        when(userRepositoryPort.existsByEmail(request.email())).thenReturn(false);
        when(userRepositoryPort.existsByName(request.name())).thenReturn(true);

        // Act & Assert
        assertThrows(UserAlreadyExistsException.class, () -> registerUserUseCase.execute(request));
        verify(userRepositoryPort, never()).save(any(User.class));
    }

    @Test
    void execute_withInvalidEmailFormat_shouldThrowDomainValidationException() {
        // Arrange
        RegisterUserRequest request = new RegisterUserRequest(
            "Alice",
            "invalid-email",
            "pwd123",
            "DELIVERY"
        );

        when(passwordEncoderPort.encode("pwd123")).thenReturn("encodedPwd123");

        // Act & Assert
        assertThrows(DomainValidationException.class, () -> registerUserUseCase.execute(request));
        verify(userRepositoryPort, never()).save(any(User.class));
    }

    @Test
    void execute_withBlankName_shouldThrowDomainValidationException() {
        // Arrange
        RegisterUserRequest request = new RegisterUserRequest(
            "",
            "alice@example.com",
            "pwd123",
            "DELIVERY"
        );

        when(passwordEncoderPort.encode("pwd123")).thenReturn("encodedPwd123");

        // Act & Assert
        assertThrows(DomainValidationException.class, () -> registerUserUseCase.execute(request));
        verify(userRepositoryPort, never()).save(any(User.class));
    }

    @Test
    void execute_withInvalidRole_shouldThrowDomainValidationException() {
        // Arrange
        RegisterUserRequest request = new RegisterUserRequest(
            "Alice",
            "alice@example.com",
            "pwd123",
            "CUSTOMER" // Invalid role, rejected by domain design
        );

        // Act & Assert
        assertThrows(DomainValidationException.class, () -> registerUserUseCase.execute(request));
        verify(userRepositoryPort, never()).save(any(User.class));
    }

    @Test
    void execute_withNullRole_shouldThrowDomainValidationException() {
        // Arrange
        RegisterUserRequest request = new RegisterUserRequest(
            "Alice",
            "alice@example.com",
            "pwd123",
            null
        );

        // Act & Assert
        assertThrows(DomainValidationException.class, () -> registerUserUseCase.execute(request));
        verify(userRepositoryPort, never()).save(any(User.class));
    }
}
