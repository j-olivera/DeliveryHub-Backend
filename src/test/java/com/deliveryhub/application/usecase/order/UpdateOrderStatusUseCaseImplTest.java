package com.deliveryhub.application.usecase.order;

import com.deliveryhub.application.port.out.order.OrderRepositoryPort;
import com.deliveryhub.domain.enums.OrderStatus;
import com.deliveryhub.domain.enums.UserRole;
import com.deliveryhub.domain.model.Address;
import com.deliveryhub.domain.model.Order;
import com.deliveryhub.domain.exception.OrderNotFoundException;
import com.deliveryhub.domain.exception.UnauthorizedTransitionException;
import com.deliveryhub.domain.exception.InvalidStatusTransitionException;
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
class UpdateOrderStatusUseCaseImplTest {

    @Mock
    private OrderRepositoryPort orderRepository;

    @Mock
    private com.deliveryhub.application.port.out.order.OrderEventPublisherPort orderEventPublisherPort;

    @InjectMocks
    private UpdateOrderStatusUseCaseImpl updateOrderStatusUseCase;

    private UUID orderId;
    private UUID deliveryUserId;
    private UUID adminUserId;
    private Order order;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();
        deliveryUserId = UUID.randomUUID();
        adminUserId = UUID.randomUUID();
        Address address = new Address("Av. Rivadavia 1234", "Buenos Aires", -34.6037, -58.3816);
        order = Order.create(adminUserId, "Juan", "+5491112345678", address, "Notes");
    }

    @Test
    void execute_whenAdminCancels_shouldCancelAndSave() {
        // Arrange
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Order result = updateOrderStatusUseCase.execute(orderId, OrderStatus.CANCELLED, adminUserId, "ADMIN");

        // Assert
        assertNotNull(result);
        assertEquals(OrderStatus.CANCELLED, result.getStatus());
        verify(orderRepository).save(any());
    }

    @Test
    void execute_whenDeliveryStartsDelivery_shouldChangeStatusToInProgress() {
        // Arrange
        order.assign(deliveryUserId, UserRole.ADMIN);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Order result = updateOrderStatusUseCase.execute(orderId, OrderStatus.IN_PROGRESS, deliveryUserId, "DELIVERY");

        // Assert
        assertNotNull(result);
        assertEquals(OrderStatus.IN_PROGRESS, result.getStatus());
        verify(orderRepository).save(any());
    }

    @Test
    void execute_whenUnassignedDeliveryStartsDelivery_shouldThrowUnauthorizedTransitionException() {
        // Arrange
        order.assign(deliveryUserId, UserRole.ADMIN);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // Act & Assert
        assertThrows(UnauthorizedTransitionException.class, () -> 
            updateOrderStatusUseCase.execute(orderId, OrderStatus.IN_PROGRESS, UUID.randomUUID(), "DELIVERY")
        );
    }

    @Test
    void execute_whenInvalidRoleForTransition_shouldThrowInvalidStatusTransitionException() {
        // Arrange
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // Act & Assert
        assertThrows(InvalidStatusTransitionException.class, () -> 
            updateOrderStatusUseCase.execute(orderId, OrderStatus.IN_PROGRESS, adminUserId, "ADMIN")
        );
    }
}
