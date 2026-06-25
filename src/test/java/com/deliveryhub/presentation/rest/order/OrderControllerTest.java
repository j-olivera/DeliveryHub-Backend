package com.deliveryhub.presentation.rest.order;

import com.deliveryhub.presentation.rest.GlobalExceptionHandler;

import com.deliveryhub.application.port.in.order.AssignOrderUseCase;
import com.deliveryhub.application.port.in.order.CreateOrderUseCase;
import com.deliveryhub.application.port.in.order.GetOrdersUseCase;
import com.deliveryhub.application.port.in.order.UpdateOrderStatusUseCase;
import com.deliveryhub.application.port.out.user.UserRepositoryPort;
import com.deliveryhub.domain.enums.OrderStatus;
import com.deliveryhub.domain.enums.UserRole;
import com.deliveryhub.domain.exception.DomainValidationException;
import com.deliveryhub.domain.exception.InvalidStatusTransitionException;
import com.deliveryhub.domain.exception.OrderNotFoundException;
import com.deliveryhub.domain.exception.UnauthorizedTransitionException;
import com.deliveryhub.domain.model.Address;
import com.deliveryhub.domain.model.Order;
import com.deliveryhub.domain.model.User;
import com.deliveryhub.presentation.dto.order.*;
import com.deliveryhub.presentation.dto.user.*;
import com.deliveryhub.presentation.mapper.order.OrderPresentationMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = OrderController.class)
@Import({OrderPresentationMapper.class, GlobalExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = false)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private CreateOrderUseCase createOrderUseCase;

    @MockitoBean
    private AssignOrderUseCase assignOrderUseCase;

    @MockitoBean
    private UpdateOrderStatusUseCase updateOrderStatusUseCase;

    @MockitoBean
    private GetOrdersUseCase getOrdersUseCase;

    @MockitoBean
    private UserRepositoryPort userRepositoryPort;

    @MockitoBean
    private com.deliveryhub.infrastructure.security.JwtUtils jwtUtils;

    @MockitoBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    @MockitoBean
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    private User adminUser;
    private User deliveryUser;
    private Order pendingOrder;

    @BeforeEach
    void setUp() {
        UUID adminId = UUID.randomUUID();
        adminUser = User.reconstruct(adminId, "Admin User", "admin@deliveryhub.com", "pwd", UserRole.ADMIN, true);

        UUID deliveryId = UUID.randomUUID();
        deliveryUser = User.reconstruct(deliveryId, "Delivery User", "delivery@deliveryhub.com", "pwd", UserRole.DELIVERY, true);

        Address address = new Address("Av. Rivadavia 1234", "Buenos Aires", -34.6037, -58.3816);
        pendingOrder = Order.reconstruct(
            UUID.randomUUID(),
            adminId,
            "Juan García",
            "+5491112345678",
            address,
            "Notes test",
            OrderStatus.PENDING,
            null,
            LocalDateTime.now(),
            null,
            null,
            LocalDateTime.now()
        );
    }

    @Test
    @WithMockUser(username = "admin@deliveryhub.com", roles = "ADMIN")
    void createOrder_whenRequestIsValid_shouldReturn201AndCreatedOrder() throws Exception {
        // Arrange
        CreateOrderRequest request = new CreateOrderRequest(
            "Juan García",
            "+5491112345678",
            new AddressRequest("Av. Rivadavia 1234", "Buenos Aires", new CoordinatesDto(-34.6037, -58.3816)),
            "Notes test"
        );

        when(userRepositoryPort.findByEmail("admin@deliveryhub.com")).thenReturn(Optional.of(adminUser));
        when(createOrderUseCase.execute(
            eq(adminUser.getId()),
            eq("Juan García"),
            eq("+5491112345678"),
            any(Address.class),
            eq("Notes test")
        )).thenReturn(pendingOrder);

        // Act & Assert
        mockMvc.perform(post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(pendingOrder.getId().toString()))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    @WithMockUser(username = "admin@deliveryhub.com", roles = "ADMIN")
    void createOrder_whenRequestIsInvalid_shouldReturn400BadRequest() throws Exception {
        // Arrange (blank customerName, null address)
        CreateOrderRequest request = new CreateOrderRequest(
            "",
            "+5491112345678",
            null,
            "Notes test"
        );

        // Act & Assert
        mockMvc.perform(post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "admin@deliveryhub.com", roles = "ADMIN")
    void assignOrder_whenRequestIsValid_shouldReturn200AndAssignedOrder() throws Exception {
        // Arrange
        UUID orderId = pendingOrder.getId();
        UUID deliveryId = deliveryUser.getId();
        AssignOrderRequest request = new AssignOrderRequest(deliveryId);

        Address address = new Address("Av. Rivadavia 1234", "Buenos Aires", -34.6037, -58.3816);
        Order assignedOrder = Order.reconstruct(
            orderId,
            adminUser.getId(),
            "Juan García",
            "+5491112345678",
            address,
            "Notes test",
            OrderStatus.ASSIGNED,
            deliveryId,
            pendingOrder.getCreatedAt(),
            LocalDateTime.now(),
            null,
            LocalDateTime.now()
        );

        when(userRepositoryPort.findByEmail("admin@deliveryhub.com")).thenReturn(Optional.of(adminUser));
        when(assignOrderUseCase.execute(orderId, deliveryId, adminUser.getId())).thenReturn(assignedOrder);

        // Act & Assert
        mockMvc.perform(patch("/api/v1/orders/{id}/assign", orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId.toString()))
                .andExpect(jsonPath("$.status").value("ASSIGNED"))
                .andExpect(jsonPath("$.deliveryUserId").value(deliveryId.toString()));
    }

    @Test
    @WithMockUser(username = "delivery@deliveryhub.com", roles = "DELIVERY")
    void updateStatus_whenRequestIsValid_shouldReturn200AndUpdatedOrder() throws Exception {
        // Arrange
        UUID orderId = pendingOrder.getId();
        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest("IN_PROGRESS");

        Address address = new Address("Av. Rivadavia 1234", "Buenos Aires", -34.6037, -58.3816);
        Order updatedOrder = Order.reconstruct(
            orderId,
            adminUser.getId(),
            "Juan García",
            "+5491112345678",
            address,
            "Notes test",
            OrderStatus.IN_PROGRESS,
            deliveryUser.getId(),
            pendingOrder.getCreatedAt(),
            LocalDateTime.now(),
            null,
            LocalDateTime.now()
        );

        when(userRepositoryPort.findByEmail("delivery@deliveryhub.com")).thenReturn(Optional.of(deliveryUser));
        when(updateOrderStatusUseCase.execute(orderId, OrderStatus.IN_PROGRESS, deliveryUser.getId(), "DELIVERY")).thenReturn(updatedOrder);

        // Act & Assert
        mockMvc.perform(patch("/api/v1/orders/{id}/status", orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId.toString()))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    @WithMockUser(username = "delivery@deliveryhub.com", roles = "DELIVERY")
    void updateStatus_whenInvalidStatusValue_shouldReturn400BadRequest() throws Exception {
        // Arrange
        UUID orderId = pendingOrder.getId();
        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest("INVALID_STATUS_ENUM");

        when(userRepositoryPort.findByEmail("delivery@deliveryhub.com")).thenReturn(Optional.of(deliveryUser));

        // Act & Assert
        mockMvc.perform(patch("/api/v1/orders/{id}/status", orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid status value: INVALID_STATUS_ENUM"));
    }

    @Test
    @WithMockUser(username = "admin@deliveryhub.com", roles = "ADMIN")
    void cancelOrder_shouldReturn204NoContent() throws Exception {
        // Arrange
        UUID orderId = pendingOrder.getId();

        when(userRepositoryPort.findByEmail("admin@deliveryhub.com")).thenReturn(Optional.of(adminUser));

        // Act & Assert
        mockMvc.perform(delete("/api/v1/orders/{id}", orderId))
                .andExpect(status().isNoContent());

        verify(updateOrderStatusUseCase).execute(orderId, OrderStatus.CANCELLED, adminUser.getId(), "ADMIN");
    }

    @Test
    @WithMockUser(username = "admin@deliveryhub.com", roles = "ADMIN")
    void getOrders_whenAdminRequests_shouldReturnPagedOrders() throws Exception {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> expectedPage = new PageImpl<>(List.of(pendingOrder));

        when(userRepositoryPort.findByEmail("admin@deliveryhub.com")).thenReturn(Optional.of(adminUser));
        when(getOrdersUseCase.execute(adminUser.getId(), "ADMIN", null, pageable)).thenReturn(expectedPage);

        // Act & Assert
        mockMvc.perform(get("/api/v1/orders")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(pendingOrder.getId().toString()))
                .andExpect(jsonPath("$.content[0].customerName").value("Juan García"))
                .andExpect(jsonPath("$.content[0].status").value("PENDING"))
                .andExpect(jsonPath("$.content[0].address.street").value("Av. Rivadavia 1234"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @WithMockUser(username = "admin@deliveryhub.com", roles = "ADMIN")
    void getOrders_whenInvalidStatusFilter_shouldReturn400BadRequest() throws Exception {
        // Arrange
        when(userRepositoryPort.findByEmail("admin@deliveryhub.com")).thenReturn(Optional.of(adminUser));

        // Act & Assert
        mockMvc.perform(get("/api/v1/orders")
                .param("status", "INVALID_STATUS_FILTER"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid status filter: INVALID_STATUS_FILTER"));
    }

    @Test
    @WithMockUser(username = "admin@deliveryhub.com", roles = "ADMIN")
    void handleOrderNotFoundException_shouldReturn404NotFound() throws Exception {
        // Arrange
        UUID orderId = UUID.randomUUID();
        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest("CANCELLED");

        when(userRepositoryPort.findByEmail("admin@deliveryhub.com")).thenReturn(Optional.of(adminUser));
        when(updateOrderStatusUseCase.execute(orderId, OrderStatus.CANCELLED, adminUser.getId(), "ADMIN"))
                .thenThrow(new OrderNotFoundException(orderId));

        // Act & Assert
        mockMvc.perform(patch("/api/v1/orders/{id}/status", orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Order with ID " + orderId + " not found"));
    }

    @Test
    @WithMockUser(username = "delivery@deliveryhub.com", roles = "DELIVERY")
    void handleUnauthorizedTransitionException_shouldReturn403Forbidden() throws Exception {
        // Arrange
        UUID orderId = pendingOrder.getId();
        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest("IN_PROGRESS");

        when(userRepositoryPort.findByEmail("delivery@deliveryhub.com")).thenReturn(Optional.of(deliveryUser));
        when(updateOrderStatusUseCase.execute(orderId, OrderStatus.IN_PROGRESS, deliveryUser.getId(), "DELIVERY"))
                .thenThrow(new UnauthorizedTransitionException("The delivery user is not assigned to this order"));

        // Act & Assert
        mockMvc.perform(patch("/api/v1/orders/{id}/status", orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("The delivery user is not assigned to this order"));
    }

    @Test
    @WithMockUser(username = "delivery@deliveryhub.com", roles = "DELIVERY")
    void handleInvalidStatusTransitionException_shouldReturn422UnprocessableEntity() throws Exception {
        // Arrange
        UUID orderId = pendingOrder.getId();
        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest("DELIVERED");

        when(userRepositoryPort.findByEmail("delivery@deliveryhub.com")).thenReturn(Optional.of(deliveryUser));
        when(updateOrderStatusUseCase.execute(orderId, OrderStatus.DELIVERED, deliveryUser.getId(), "DELIVERY"))
                .thenThrow(new InvalidStatusTransitionException("Cannot complete delivery of non-started order"));

        // Act & Assert
        mockMvc.perform(patch("/api/v1/orders/{id}/status", orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("Cannot complete delivery of non-started order"));
    }
}
