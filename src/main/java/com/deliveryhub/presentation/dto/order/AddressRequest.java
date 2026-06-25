package com.deliveryhub.presentation.dto.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO que representa la dirección dentro de una solicitud de pedido.
 */
public record AddressRequest(
    @NotBlank(message = "Street cannot be blank")
    String street,

    @NotBlank(message = "City cannot be blank")
    String city,

    @NotNull(message = "Coordinates are required")
    @jakarta.validation.Valid
    CoordinatesDto coordinates
) {}
