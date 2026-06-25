package com.deliveryhub.presentation.dto.order;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * DTO que representa la solicitud de asignación de un repartidor a un pedido.
 */
public record AssignOrderRequest(
    @NotNull(message = "Delivery user ID is required")
    UUID deliveryUserId
) {}
