package com.deliveryhub.presentation.rest.user;

import com.deliveryhub.application.port.in.user.RegisterUserUseCase;
import com.deliveryhub.domain.enums.UserRole;
import com.deliveryhub.domain.model.User;
import com.deliveryhub.presentation.dto.user.RegisterUserRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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
    controllers = UserController.class
)
@org.springframework.context.annotation.Import(com.deliveryhub.presentation.mapper.user.UserPresentationMapper.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private RegisterUserUseCase registerUserUseCase;

    @MockitoBean
    private com.deliveryhub.application.port.in.user.GetActiveDeliveriesUseCase getActiveDeliveriesUseCase;

    @MockitoBean
    private com.deliveryhub.infrastructure.security.JwtUtils jwtUtils;

    @MockitoBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    @MockitoBean
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Test
    void registerUser_whenRequestIsValid_shouldReturn201AndCreatedUser() throws Exception {
        // Arrange
        RegisterUserRequest request = new RegisterUserRequest(
            "Alice",
            "alice@example.com",
            "pwd123"
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
            "pwd123"
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
            "pwd123"
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
            "pwd123"
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
            ""
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
            "pwd123"
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
            "pwd123"
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

    @Test
    void getActiveDeliveries_shouldReturn200AndRidersList() throws Exception {
        // Arrange
        java.util.UUID riderId = java.util.UUID.randomUUID();
        User rider = User.reconstruct(riderId, "Carlos", "carlos@test.com", "pwd123", UserRole.DELIVERY);
        when(getActiveDeliveriesUseCase.execute()).thenReturn(java.util.List.of(rider));

        // Act & Assert
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(riderId.toString()))
                .andExpect(jsonPath("$[0].name").value("Carlos"))
                .andExpect(jsonPath("$[0].email").value("carlos@test.com"))
                .andExpect(jsonPath("$[0].role").value("DELIVERY"));
    }
}
