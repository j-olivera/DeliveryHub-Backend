package com.deliveryhub.application.usecase.order;

import com.deliveryhub.application.port.out.order.OrderRepositoryPort;
import com.deliveryhub.application.port.out.user.UserRepositoryPort;
import com.deliveryhub.domain.enums.OrderStatus;
import com.deliveryhub.domain.enums.UserRole;
import com.deliveryhub.domain.model.Address;
import com.deliveryhub.domain.model.Order;
import com.deliveryhub.domain.model.User;
import com.deliveryhub.domain.exception.DomainValidationException;
import com.deliveryhub.domain.exception.OrderNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssignOrderUseCaseImplTest {

    @Mock
    private OrderRepositoryPort orderRepository;

    @Mock
    private UserRepositoryPort userRepository;

    @InjectMocks
    private AssignOrderUseCaseImpl assignOrderUseCase;

    private UUID orderId;
    private UUID deliveryUserId;
    private UUID adminUserId;
    private Order order;
    private User deliveryUser;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();
        deliveryUserId = UUID.randomUUID();
        adminUserId = UUID.randomUUID();

        Address address = new Address("Av. Rivadavia 1234", "Buenos Aires", -34.6037, -58.3816);
        order = Order.create(adminUserId, "Juan", "+5491112345678", address, "Notes");
        deliveryUser = User.reconstruct(deliveryUserId, "Carlos", "carlos@test.com", "pwd", UserRole.DELIVERY);
    }

    @Test
    void execute_whenDataIsValid_shouldAssignAndSave() {
        // Arrange
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(userRepository.findById(deliveryUserId)).thenReturn(Optional.of(deliveryUser));
        when(orderRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Order result = assignOrderUseCase.execute(orderId, deliveryUserId, adminUserId);

        // Assert
        assertNotNull(result);
        assertEquals(OrderStatus.ASSIGNED, result.getStatus());
        assertEquals(deliveryUserId, result.getDeliveryUserId());
        verify(orderRepository).save(any());
    }

    @Test
    void execute_whenOrderNotFound_shouldThrowOrderNotFoundException() {
        // Arrange
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(OrderNotFoundException.class, () -> 
            assignOrderUseCase.execute(orderId, deliveryUserId, adminUserId)
        );
    }

    @Test
    void execute_whenDeliveryUserNotFound_shouldThrowDomainValidationException() {
        // Arrange
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(userRepository.findById(deliveryUserId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(DomainValidationException.class, () -> 
            assignOrderUseCase.execute(orderId, deliveryUserId, adminUserId)
        );
    }

    @Test
    void execute_whenUserIsNotDelivery_shouldThrowDomainValidationException() {
        // Arrange
        User adminUser = User.reconstruct(deliveryUserId, "Carlos", "carlos@test.com", "pwd", UserRole.ADMIN);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(userRepository.findById(deliveryUserId)).thenReturn(Optional.of(adminUser));

        // Act & Assert
        assertThrows(DomainValidationException.class, () -> 
            assignOrderUseCase.execute(orderId, deliveryUserId, adminUserId)
        );
    }

    @Test
    void execute_whenUserIsNotActive_shouldThrowDomainValidationException() {
        // Arrange
        User inactiveUser = User.reconstruct(deliveryUserId, "Carlos", "carlos@test.com", "pwd", UserRole.DELIVERY);
        inactiveUser.deactivate();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(userRepository.findById(deliveryUserId)).thenReturn(Optional.of(inactiveUser));

        // Act & Assert
        assertThrows(DomainValidationException.class, () -> 
            assignOrderUseCase.execute(orderId, deliveryUserId, adminUserId)
        );
    }

    @Test
    void execute_whenUserHasActiveOrders_shouldThrowDomainValidationException() {
        // Arrange
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(userRepository.findById(deliveryUserId)).thenReturn(Optional.of(deliveryUser));
        when(orderRepository.hasActiveOrders(deliveryUserId)).thenReturn(true);

        // Act & Assert
        DomainValidationException ex = assertThrows(DomainValidationException.class, () -> 
            assignOrderUseCase.execute(orderId, deliveryUserId, adminUserId)
        );
        assertEquals("Delivery user already has active orders", ex.getMessage());
    }
}
