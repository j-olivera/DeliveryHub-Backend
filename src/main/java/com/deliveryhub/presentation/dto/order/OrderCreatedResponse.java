package com.deliveryhub.presentation.dto.order;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de respuesta para un pedido creado.
 */
public record OrderCreatedResponse(
    UUID id,
    String status,
    LocalDateTime createdAt
) {}
