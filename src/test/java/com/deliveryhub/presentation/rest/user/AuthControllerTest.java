package com.deliveryhub.presentation.rest.user;

import com.deliveryhub.application.dto.user.LoginResponse;
import com.deliveryhub.application.port.in.user.LoginUseCase;
import com.deliveryhub.domain.exception.InvalidCredentialsException;
import com.deliveryhub.presentation.dto.user.LoginRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
    controllers = AuthController.class
)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private LoginUseCase loginUseCase;

    @MockitoBean
    private com.deliveryhub.infrastructure.security.JwtUtils jwtUtils;

    @MockitoBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    @MockitoBean
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Test
    void login_whenCredentialsAreValid_shouldReturn200AndLoginResponse() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest("john@example.com", "password123");
        UUID userId = UUID.randomUUID();
        LoginResponse loginResponse = new LoginResponse("dummy-jwt-token", userId, "DELIVERY");

        when(loginUseCase.execute(request.email(), request.password())).thenReturn(loginResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("dummy-jwt-token"))
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.role").value("DELIVERY"));
    }

    @Test
    void login_whenCredentialsAreInvalid_shouldReturn401Unauthorized() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest("john@example.com", "wrong-password");

        when(loginUseCase.execute(request.email(), request.password()))
                .thenThrow(new InvalidCredentialsException("Invalid email or password"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email or password"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void login_whenEmailIsBlank_shouldReturn400BadRequest() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest("", "password123");

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_whenPasswordIsBlank_shouldReturn400BadRequest() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest("john@example.com", "");

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
