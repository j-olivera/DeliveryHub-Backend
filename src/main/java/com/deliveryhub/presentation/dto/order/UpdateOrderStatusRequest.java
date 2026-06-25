package com.deliveryhub.presentation.dto.order;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO que representa la solicitud de actualización de estado de un pedido.
 */
public record UpdateOrderStatusRequest(
    @NotBlank(message = "Status cannot be blank")
    String status
) {}
