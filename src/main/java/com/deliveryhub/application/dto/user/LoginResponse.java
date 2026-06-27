package com.deliveryhub.application.dto.user;

import java.util.UUID;

/**
 * DTO que representa la respuesta a un inicio de sesión exitoso.
 *
 * @param token  el token JWT generado
 * @param userId el ID único del usuario autenticado
 * @param role   el rol del usuario autenticado
 */
public record LoginResponse(
    String token,
    UUID userId,
    String role,
    String name
) {}
