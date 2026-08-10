package com.deliveryhub.infrastructure.websocket;

import com.deliveryhub.application.dto.order.OrderEvent;
import com.deliveryhub.domain.enums.OrderStatus;
import com.deliveryhub.domain.model.Address;
import com.deliveryhub.domain.model.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderEventPublisherAdapterTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    private OrderEventPublisherAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new OrderEventPublisherAdapter(messagingTemplate);
    }

    @Test
    void publishOrderCreated_shouldFormatTimestampInUTC() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        LocalDateTime localCreatedAt = LocalDateTime.of(2026, 7, 2, 13, 15, 44);
        Order order = Order.reconstruct(
                orderId,
                customerId,
                "Customer",
                "123",
                new Address("Calle Falsa 123", "CABA", 0, 0),
                "Notes",
                OrderStatus.PENDING,
                null,
                localCreatedAt,
                null,
                null,
                null
        );

        // Act
        adapter.publishOrderCreated(order);

        // Assert
        ArgumentCaptor<OrderEvent> eventCaptor = ArgumentCaptor.forClass(OrderEvent.class);
        verify(messagingTemplate).convertAndSend(eq("/topic/admin/orders"), eventCaptor.capture());

        OrderEvent event = eventCaptor.getValue();
        assertNotNull(event);

        ZonedDateTime localZoned = localCreatedAt.atZone(ZoneId.systemDefault());
        String expectedTimestamp = localZoned.withZoneSameInstant(ZoneOffset.UTC)
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        assertEquals(expectedTimestamp, event.timestamp());
    }
}
