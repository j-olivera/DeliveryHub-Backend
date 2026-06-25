package com.deliveryhub.application.usecase.order;

import com.deliveryhub.application.port.out.order.OrderRepositoryPort;
import com.deliveryhub.domain.model.Address;
import com.deliveryhub.domain.model.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateOrderUseCaseImplTest {

    @Mock
    private OrderRepositoryPort orderRepository;

    @Mock
    private com.deliveryhub.application.port.out.order.OrderEventPublisherPort orderEventPublisherPort;

    @InjectMocks
    private CreateOrderUseCaseImpl createOrderUseCase;

    @Test
    void execute_whenDataIsValid_shouldSaveAndReturnOrder() {
        // Arrange
        UUID adminId = UUID.randomUUID();
        Address address = new Address("Av. Rivadavia 1234", "Buenos Aires", -34.6037, -58.3816);
        Order order = Order.create(adminId, "Juan", "+5491112345678", address, "Notes");

        when(orderRepository.save(any())).thenReturn(order);

        // Act
        Order result = createOrderUseCase.execute(adminId, "Juan", "+5491112345678", 
                                                 address, "Notes");

        // Assert
        assertNotNull(result);
        assertEquals(order.getId(), result.getId());
        verify(orderRepository).save(any());
    }
}
