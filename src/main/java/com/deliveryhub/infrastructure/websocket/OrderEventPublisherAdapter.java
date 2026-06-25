package com.deliveryhub.infrastructure.websocket;

import com.deliveryhub.application.dto.order.OrderEvent;
import com.deliveryhub.application.port.out.order.OrderEventPublisherPort;
import com.deliveryhub.application.port.out.user.UserRepositoryPort;
import com.deliveryhub.domain.model.Order;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Adaptador de infraestructura que implementa OrderEventPublisherPort.
 * Envía notificaciones en tiempo real utilizando Spring SimpMessagingTemplate sobre WebSockets (STOMP).
 */
@Component
public class OrderEventPublisherAdapter implements OrderEventPublisherPort {

    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepositoryPort userRepositoryPort;

    /**
     * Constructor del adaptador.
     *
     * @param messagingTemplate plantilla de mensajería para Spring WebSockets
     * @param userRepositoryPort puerto para obtener información de usuarios
     */
    public OrderEventPublisherAdapter(SimpMessagingTemplate messagingTemplate, UserRepositoryPort userRepositoryPort) {
        this.messagingTemplate = messagingTemplate;
        this.userRepositoryPort = userRepositoryPort;
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
        String timestamp = order.getUpdatedAt() != null 
            ? order.getUpdatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) 
            : "";
        return new OrderEvent(
            eventType,
            order.getId(),
            order.getStatus().name(),
            order.getDeliveryUserId(),
            timestamp
        );
    }

    private void sendToDeliveryUser(UUID deliveryUserId, OrderEvent event) {
        // En Spring Security/WebSockets el Principal Name es el email del usuario,
        // por lo que debemos recuperar el email del repartidor por su ID para enviarle el mensaje.
        userRepositoryPort.findById(deliveryUserId).ifPresent(user -> {
            messagingTemplate.convertAndSendToUser(user.getEmail(), "/orders", event);
        });
    }
}
