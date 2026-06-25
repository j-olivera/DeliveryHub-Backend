package com.deliveryhub.application.dto.order;

import java.util.UUID;

/**
 * DTO que representa el payload de los eventos de pedidos enviados a través de WebSockets.
 */
public record OrderEvent(
    String eventType,
    UUID orderId,
    String newStatus,
    UUID deliveryUserId,
    String timestamp
) {}
