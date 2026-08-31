package com.deliveryhub.presentation.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * DTO que representa la solicitud de registro de un usuario.
 * Contiene las anotaciones de validación JSR-380 necesarias.
 *
 * @param name     nombre del usuario, no puede estar vacío
 * @param email    correo electrónico, debe ser un formato válido y no vacío
 * @param password contraseña, no puede estar vacía
 * @param role     rol del usuario, debe ser ADMIN o DELIVERY
 */
public record RegisterUserRequest(
    @NotBlank(message = "Name cannot be blank")
    String name,

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Invalid email format")
    String email,

    @NotBlank(message = "Password cannot be blank")
    String password
) {}
