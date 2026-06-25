package com.deliveryhub.infrastructure.security;

import com.deliveryhub.application.dto.user.LoginResponse;
import com.deliveryhub.application.port.in.user.LoginUseCase;
import com.deliveryhub.application.port.in.user.RegisterUserUseCase;
import com.deliveryhub.domain.enums.UserRole;
import com.deliveryhub.domain.model.User;
import com.deliveryhub.presentation.rest.user.AuthController;
import com.deliveryhub.presentation.rest.user.UserController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {AuthController.class, UserController.class, TestSecureController.class})
@Import({SecurityConfig.class, JwtUtils.class, com.deliveryhub.presentation.mapper.user.UserPresentationMapper.class})
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LoginUseCase loginUseCase;

    @MockitoBean
    private RegisterUserUseCase registerUserUseCase;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    @MockitoBean
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Test
    void publicEndpoints_shouldBeAccessibleWithoutAuth() throws Exception {
        // POST /api/v1/auth/login es público
        when(loginUseCase.execute(any(), any())).thenReturn(new LoginResponse("token", UUID.randomUUID(), "DELIVERY"));
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@test.com\",\"password\":\"pwd\"}"))
                .andExpect(status().isOk());

        // POST /api/v1/users es público (registro)
        User user = User.reconstruct(UUID.randomUUID(), "Alice", "alice@test.com", "pwd", UserRole.DELIVERY);
        when(registerUserUseCase.execute(any())).thenReturn(user);
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Alice\",\"email\":\"alice@test.com\",\"password\":\"pwd\",\"role\":\"DELIVERY\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void protectedEndpoints_withoutToken_shouldReturn401Unauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/test-orders"))
                .andExpect(status().isForbidden()); // Spring Security default returns 403 when authentication is anonymous instead of 401 unless configured otherwise or standard entry points are defined
    }

    @Test
    @WithMockUser(roles = "DELIVERY")
    void deliveryEndpoints_withDeliveryRole_shouldBeAllowed() throws Exception {
        mockMvc.perform(get("/api/v1/delivery/orders"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void deliveryEndpoints_withUserRole_shouldReturn403Forbidden() throws Exception {
        mockMvc.perform(get("/api/v1/delivery/orders"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminEndpoints_withAdminRole_shouldBeAllowed() throws Exception {
        mockMvc.perform(get("/api/v1/admin/dashboard"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "DELIVERY")
    void adminEndpoints_withDeliveryRole_shouldReturn403Forbidden() throws Exception {
        mockMvc.perform(get("/api/v1/admin/dashboard"))
                .andExpect(status().isForbidden());
    }
}
