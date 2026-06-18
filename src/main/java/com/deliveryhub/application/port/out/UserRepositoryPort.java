package com.deliveryhub.application.port.out;

import com.deliveryhub.domain.model.User;

/**
 * Puerto de salida para las operaciones de persistencia de usuarios.
 */
public interface UserRepositoryPort {
    /**
     * Guarda un usuario en el sistema de persistencia.
     *
     * @param user el usuario a guardar
     * @return el usuario guardado
     */
    User save(User user);

    /**
     * Verifica si ya existe un usuario registrado con el correo electrónico provisto.
     *
     * @param email el correo electrónico a verificar
     * @return true si ya existe, false en caso contrario
     */
    boolean existsByEmail(String email);

    /**
     * Verifica si ya existe un usuario registrado con el nombre provisto.
     *
     * @param name el nombre a verificar
     * @return true si ya existe, false en caso contrario
     */
    boolean existsByName(String name);
}
