package com.deliveryhub.application.port.in.user;

import com.deliveryhub.application.dto.user.RegisterUserRequest;
import com.deliveryhub.domain.model.User;

/**
 * Puerto de entrada que define el caso de uso para registrar un nuevo usuario.
 */
public interface RegisterUserUseCase {
    /**
     * Registra un nuevo usuario tras validar sus datos y unicidad.
     *
     * @param request datos del usuario
     * @return usuario creado y persistido
     */
    User execute(RegisterUserRequest request);
}
