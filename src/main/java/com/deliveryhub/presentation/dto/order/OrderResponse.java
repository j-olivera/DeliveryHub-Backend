package com.deliveryhub.presentation.dto.order;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO que representa los detalles completos de un pedido en las respuestas de la API.
 */
public record OrderResponse(
    UUID id,
    String customerName,
    String status,
    AddressResponse address,
    UUID deliveryUserId,
    LocalDateTime createdAt
) {}
