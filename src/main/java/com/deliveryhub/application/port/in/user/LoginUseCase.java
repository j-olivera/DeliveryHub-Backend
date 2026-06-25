package com.deliveryhub.application.port.in.user;

import com.deliveryhub.application.dto.user.LoginResponse;

/**
 * Puerto de entrada para el caso de uso de inicio de sesión (Login).
 */
public interface LoginUseCase {
    /**
     * Ejecuta el inicio de sesión para un usuario con las credenciales indicadas.
     *
     * @param email    el correo electrónico del usuario
     * @param password la contraseña en texto plano
     * @return la respuesta conteniendo el token, ID de usuario y rol
     */
    LoginResponse execute(String email, String password);
}
