package com.deliveryhub.presentation.dto.order;

/**
 * DTO de coordenadas geográficas para respuestas de la API.
 */
public record CoordinatesResponse(
    double lat,
    double lng
) {}
