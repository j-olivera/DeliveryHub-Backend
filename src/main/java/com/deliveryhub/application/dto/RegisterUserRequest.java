package com.deliveryhub.application.dto;

/**
 * Registro (DTO) que representa la solicitud para registrar un nuevo usuario.
 *
 * @param name     nombre del usuario
 * @param email    correo electrónico del usuario
 * @param password contraseña en texto plano del usuario
 * @param role     rol solicitado para el usuario
 */
public record RegisterUserRequest(
    String name,
    String email,
    String password,
    String role
) {}
