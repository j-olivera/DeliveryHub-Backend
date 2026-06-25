package com.deliveryhub.presentation.dto.order;

import jakarta.validation.constraints.NotNull;

/**
 * DTO que representa las coordenadas geográficas de latitud y longitud.
 */
public record CoordinatesDto(
    @NotNull(message = "Latitude is required")
    Double lat,

    @NotNull(message = "Longitude is required")
    Double lng
) {}
