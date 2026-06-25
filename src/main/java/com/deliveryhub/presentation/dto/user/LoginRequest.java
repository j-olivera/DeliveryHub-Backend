package com.deliveryhub.presentation.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO que representa la solicitud de inicio de sesión.
 *
 * @param email    el correo electrónico del usuario
 * @param password la contraseña en texto plano
 */
public record LoginRequest(
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    String email,

    @NotBlank(message = "Password is required")
    String password
) {}
