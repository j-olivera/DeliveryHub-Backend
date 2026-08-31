package com.deliveryhub.presentation.mapper.user;

import com.deliveryhub.domain.enums.UserRole;
import com.deliveryhub.domain.model.User;
import com.deliveryhub.presentation.dto.user.RegisterUserRequest;
import com.deliveryhub.presentation.dto.user.UserResponse;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserPresentationMapperTest {

    private final UserPresentationMapper mapper = new UserPresentationMapper();

    @Test
    void toApplicationRequest_whenRequestIsValid_shouldMapFieldsCorrectly() {
        // Arrange
        RegisterUserRequest request = new RegisterUserRequest(
            "John Doe",
            "john@example.com",
            "password123"
        );

        // Act
        com.deliveryhub.application.dto.user.RegisterUserRequest result = mapper.toApplicationRequest(request);

        // Assert
        assertNotNull(result);
        assertEquals(request.name(), result.name());
        assertEquals(request.email(), result.email());
        assertEquals(request.password(), result.password());
        assertEquals("DELIVERY", result.role());
    }

    @Test
    void toApplicationRequest_whenRequestIsNull_shouldReturnNull() {
        // Act & Assert
        assertNull(mapper.toApplicationRequest(null));
    }

    @Test
    void toResponse_whenUserIsValid_shouldMapFieldsCorrectly() {
        // Arrange
        UUID id = UUID.randomUUID();
        User domain = User.reconstruct(id, "John Doe", "john@example.com", "password123", UserRole.ADMIN);

        // Act
        UserResponse response = mapper.toResponse(domain);

        // Assert
        assertNotNull(response);
        assertEquals(domain.getId(), response.id());
        assertEquals(domain.getName(), response.name());
        assertEquals(domain.getEmail(), response.email());
        assertEquals(domain.getRole().name(), response.role());
    }

    @Test
    void toResponse_whenUserIsNull_shouldReturnNull() {
        // Act & Assert
        assertNull(mapper.toResponse(null));
    }
}
