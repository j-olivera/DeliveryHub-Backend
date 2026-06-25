package com.deliveryhub.infrastructure.security;

import com.deliveryhub.application.port.out.user.UserRepositoryPort;
import com.deliveryhub.domain.enums.UserRole;
import com.deliveryhub.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;

    private UserDetailsServiceImpl userDetailsService;

    @BeforeEach
    void setUp() {
        userDetailsService = new UserDetailsServiceImpl(userRepositoryPort);
    }

    @Test
    void loadUserByUsername_whenUserExists_shouldReturnUserDetails() {
        // Arrange
        String email = "test@deliveryhub.com";
        User user = User.create("Test User", email, "encodedPassword", UserRole.DELIVERY);
        when(userRepositoryPort.findByEmail(email)).thenReturn(Optional.of(user));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        // Assert
        assertNotNull(userDetails);
        assertEquals(email, userDetails.getUsername());
        assertEquals("encodedPassword", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_DELIVERY")));
        verify(userRepositoryPort).findByEmail(email);
    }

    @Test
    void loadUserByUsername_whenUserDoesNotExist_shouldThrowUsernameNotFoundException() {
        // Arrange
        String email = "nonexistent@deliveryhub.com";
        when(userRepositoryPort.findByEmail(email)).thenReturn(Optional.empty());

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername(email));

        assertEquals("User not found with email: " + email, exception.getMessage());
        verify(userRepositoryPort).findByEmail(email);
    }
}
