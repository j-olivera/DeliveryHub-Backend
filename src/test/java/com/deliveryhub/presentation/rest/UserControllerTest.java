package com.deliveryhub.presentation.rest;

import com.deliveryhub.application.port.in.RegisterUserUseCase;
import com.deliveryhub.domain.enums.UserRole;
import com.deliveryhub.domain.model.User;
import com.deliveryhub.presentation.dto.RegisterUserRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
    controllers = UserController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class, UserDetailsServiceAutoConfiguration.class}
)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private RegisterUserUseCase registerUserUseCase;

    @Test
    void registerUser_whenRequestIsValid_shouldReturn201AndCreatedUser() throws Exception {
        // Arrange
        RegisterUserRequest request = new RegisterUserRequest(
            "Alice",
            "alice@example.com",
            "pwd123",
            "DELIVERY"
        );
        UUID userId = UUID.randomUUID();
        User createdUser = User.reconstruct(userId, "Alice", "alice@example.com", "pwd123", UserRole.DELIVERY);

        when(registerUserUseCase.execute(any())).thenReturn(createdUser);

        // Act & Assert
        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.name").value("Alice"))
                .andExpect(jsonPath("$.email").value("alice@example.com"))
                .andExpect(jsonPath("$.role").value("DELIVERY"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void registerUser_whenNameIsBlank_shouldReturn400BadRequest() throws Exception {
        RegisterUserRequest request = new RegisterUserRequest(
            "",
            "alice@example.com",
            "pwd123",
            "DELIVERY"
        );

        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Name cannot be blank"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void registerUser_whenEmailIsBlank_shouldReturn400BadRequest() throws Exception {
        RegisterUserRequest request = new RegisterUserRequest(
            "Alice",
            " ",
            "pwd123",
            "DELIVERY"
        );

        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerUser_whenEmailIsInvalid_shouldReturn400BadRequest() throws Exception {
        RegisterUserRequest request = new RegisterUserRequest(
            "Alice",
            "invalid-email",
            "pwd123",
            "DELIVERY"
        );

        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerUser_whenPasswordIsBlank_shouldReturn400BadRequest() throws Exception {
        RegisterUserRequest request = new RegisterUserRequest(
            "Alice",
            "alice@example.com",
            "",
            "DELIVERY"
        );

        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerUser_whenRoleIsBlank_shouldReturn400BadRequest() throws Exception {
        RegisterUserRequest request = new RegisterUserRequest(
            "Alice",
            "alice@example.com",
            "pwd123",
            ""
        );

        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerUser_whenRoleIsInvalid_shouldReturn400BadRequest() throws Exception {
        RegisterUserRequest request = new RegisterUserRequest(
            "Alice",
            "alice@example.com",
            "pwd123",
            "INVALID_ROLE"
        );

        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerUser_whenUserAlreadyExists_shouldReturn409Conflict() throws Exception {
        RegisterUserRequest request = new RegisterUserRequest(
            "Alice",
            "alice@example.com",
            "pwd123",
            "DELIVERY"
        );

        when(registerUserUseCase.execute(any()))
            .thenThrow(new com.deliveryhub.domain.exception.UserAlreadyExistsException("User already exists"));

        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("User already exists"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void registerUser_whenDomainValidationFails_shouldReturn400BadRequestAndErrorResponse() throws Exception {
        RegisterUserRequest request = new RegisterUserRequest(
            "Alice",
            "alice@example.com",
            "pwd123",
            "DELIVERY"
        );

        when(registerUserUseCase.execute(any()))
            .thenThrow(new com.deliveryhub.domain.exception.DomainValidationException("Domain validation failed"));

        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Domain validation failed"))
                .andExpect(jsonPath("$.timestamp").exists());
    }
}
