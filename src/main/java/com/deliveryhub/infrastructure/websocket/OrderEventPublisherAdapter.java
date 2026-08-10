package com.deliveryhub.infrastructure.websocket;

import com.deliveryhub.application.dto.order.OrderEvent;
import com.deliveryhub.application.port.out.order.OrderEventPublisherPort;
import com.deliveryhub.domain.model.Order;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Adaptador de infraestructura que implementa OrderEventPublisherPort.
 * Envía notificaciones en tiempo real utilizando Spring SimpMessagingTemplate sobre WebSockets (STOMP).
 */
@Component
public class OrderEventPublisherAdapter implements OrderEventPublisherPort {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Constructor del adaptador.
     *
     * @param messagingTemplate plantilla de mensajería para Spring WebSockets
     */
    public OrderEventPublisherAdapter(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void publishOrderCreated(Order order) {
        OrderEvent event = createEvent("ORDER_CREATED", order);
        // Notificar a ADMIN
        messagingTemplate.convertAndSend("/topic/admin/orders", event);
    }

    @Override
    public void publishOrderAssigned(Order order) {
        OrderEvent event = createEvent("ORDER_ASSIGNED", order);
        // Notificar a ADMIN
        messagingTemplate.convertAndSend("/topic/admin/orders", event);

        // Notificar al repartidor asignado (usando su email como Principal Name de la sesión WebSocket)
        if (order.getDeliveryUserId() != null) {
            sendToDeliveryUser(order.getDeliveryUserId(), event);
        }
    }

    @Override
    public void publishOrderUpdated(Order order) {
        OrderEvent event = createEvent("ORDER_STATUS_UPDATED", order);
        // Notificar a ADMIN
        messagingTemplate.convertAndSend("/topic/admin/orders", event);

        // Notificar al repartidor asignado (usando su email como Principal Name de la sesión WebSocket)
        if (order.getDeliveryUserId() != null) {
            sendToDeliveryUser(order.getDeliveryUserId(), event);
        }
    }

    @Override
    public void publishOrderCancelled(Order order) {
        OrderEvent event = createEvent("ORDER_CANCELLED", order);
        // Notificar a ADMIN
        messagingTemplate.convertAndSend("/topic/admin/orders", event);

        // Notificar al repartidor si tenía uno asignado
        if (order.getDeliveryUserId() != null) {
            sendToDeliveryUser(order.getDeliveryUserId(), event);
        }
    }

    private OrderEvent createEvent(String eventType, Order order) {
        LocalDateTime eventTime = order.getUpdatedAt() != null ? order.getUpdatedAt() : order.getCreatedAt();
        if (eventTime == null) {
            eventTime = LocalDateTime.now();
        }
        String timestamp = eventTime.atZone(ZoneId.systemDefault())
                .withZoneSameInstant(ZoneOffset.UTC)
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        return new OrderEvent(
            eventType,
            order.getId(),
            order.getStatus().name(),
            order.getDeliveryUserId(),
            timestamp
        );
    }

    private void sendToDeliveryUser(UUID deliveryUserId, OrderEvent event) {
        // Enviar el mensaje a /queue/orders utilizando deliveryUserId.toString() como el Principal Name directamente, sin consultar el repositorio.
        messagingTemplate.convertAndSendToUser(deliveryUserId.toString(), "/queue/orders", event);
    }
}
