package com.deliveryhub.presentation.dto.order;

/**
 * DTO que representa la dirección de entrega en las respuestas de la API.
 */
public record AddressResponse(
    String street,
    String city,
    CoordinatesResponse coordinates
) {}
