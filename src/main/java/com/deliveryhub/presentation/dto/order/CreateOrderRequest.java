package com.deliveryhub.presentation.dto.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO que representa la solicitud de creación de un nuevo pedido.
 */
public record CreateOrderRequest(
    @NotBlank(message = "Customer name cannot be blank")
    String customerName,

    String customerPhone,

    @NotNull(message = "Address is required")
    @Valid
    AddressRequest address,

    String notes
) {}
