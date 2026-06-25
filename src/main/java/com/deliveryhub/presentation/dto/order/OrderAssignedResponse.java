package com.deliveryhub.presentation.dto.order;

import java.util.UUID;

/**
 * DTO de respuesta para un pedido asignado.
 */
public record OrderAssignedResponse(
    UUID id,
    String status,
    UUID deliveryUserId
) {}
