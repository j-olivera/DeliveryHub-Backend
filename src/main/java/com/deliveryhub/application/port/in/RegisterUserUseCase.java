package com.deliveryhub.application.port.in;

import com.deliveryhub.application.dto.RegisterUserRequest;
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
