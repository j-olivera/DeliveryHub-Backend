package com.deliveryhub.presentation.dto;

import java.util.UUID;

/**
 * DTO que representa la respuesta tras el registro de un usuario.
 * No contiene la contraseña para mantener la seguridad.
 *
 * @param id    identificador único del usuario
 * @param name  nombre del usuario
 * @param email correo electrónico del usuario
 * @param role  rol asignado al usuario
 */
public record UserResponse(
    UUID id,
    String name,
    String email,
    String role
) {}
