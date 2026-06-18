package com.deliveryhub.presentation.dto;

import java.time.LocalDateTime;

/**
 * DTO estandarizado para representar las respuestas de error en la API.
 *
 * @param message   detalle descriptivo del error
 * @param timestamp fecha y hora en la que ocurrió el error
 */
public record ErrorResponse(
    String message,
    LocalDateTime timestamp
) {}
