package com.deliveryhub.application.port.out.user;

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

    /**
     * Busca un usuario por su correo electrónico.
     *
     * @param email el correo electrónico del usuario
     * @return un Optional conteniendo el usuario si se encuentra, o vacío si no
     */
    java.util.Optional<User> findByEmail(String email);

    /**
     * Busca un usuario por su ID.
     *
     * @param id el ID del usuario
     * @return un Optional conteniendo el usuario si se encuentra, o vacío si no
     */
    java.util.Optional<User> findById(java.util.UUID id);

    /**
     * Busca todos los usuarios activos que tienen el rol de DELIVERY.
     *
     * @return lista de repartidores activos
     */
    java.util.List<User> findActiveDeliveries();
}
