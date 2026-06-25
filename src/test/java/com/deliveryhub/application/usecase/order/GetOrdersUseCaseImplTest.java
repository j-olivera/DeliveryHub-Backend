package com.deliveryhub.application.usecase.order;

import com.deliveryhub.application.port.out.order.OrderRepositoryPort;
import com.deliveryhub.domain.enums.OrderStatus;
import com.deliveryhub.domain.model.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetOrdersUseCaseImplTest {

    @Mock
    private OrderRepositoryPort orderRepository;

    @InjectMocks
    private GetOrdersUseCaseImpl getOrdersUseCase;

    @Test
    void execute_whenRequesterIsAdminWithoutStatusFilter_shouldCallFindAll() {
        // Arrange
        UUID adminId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> expectedPage = new PageImpl<>(List.of());

        when(orderRepository.findAll(pageable)).thenReturn(expectedPage);

        // Act
        Page<Order> result = getOrdersUseCase.execute(adminId, "ADMIN", null, pageable);

        // Assert
        assertNotNull(result);
        verify(orderRepository).findAll(pageable);
        verify(orderRepository, never()).findAllByStatus(any(), any());
    }

    @Test
    void execute_whenRequesterIsAdminWithStatusFilter_shouldCallFindAllByStatus() {
        // Arrange
        UUID adminId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> expectedPage = new PageImpl<>(List.of());

        when(orderRepository.findAllByStatus(OrderStatus.PENDING, pageable)).thenReturn(expectedPage);

        // Act
        Page<Order> result = getOrdersUseCase.execute(adminId, "ADMIN", OrderStatus.PENDING, pageable);

        // Assert
        assertNotNull(result);
        verify(orderRepository).findAllByStatus(OrderStatus.PENDING, pageable);
        verify(orderRepository, never()).findAll(any());
    }

    @Test
    void execute_whenRequesterIsDeliveryWithoutStatusFilter_shouldCallFindAllByDeliveryUserId() {
        // Arrange
        UUID deliveryId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> expectedPage = new PageImpl<>(List.of());

        when(orderRepository.findAllByDeliveryUserId(deliveryId, pageable)).thenReturn(expectedPage);

        // Act
        Page<Order> result = getOrdersUseCase.execute(deliveryId, "DELIVERY", null, pageable);

        // Assert
        assertNotNull(result);
        verify(orderRepository).findAllByDeliveryUserId(deliveryId, pageable);
    }
}
