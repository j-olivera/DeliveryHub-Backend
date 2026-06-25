package com.deliveryhub.presentation.dto.order;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de respuesta para un pedido con estado actualizado.
 */
public record OrderStatusUpdatedResponse(
    UUID id,
    String status,
    LocalDateTime updatedAt
) {}
