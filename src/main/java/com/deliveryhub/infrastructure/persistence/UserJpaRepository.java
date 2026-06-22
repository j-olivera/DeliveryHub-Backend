package com.deliveryhub.infrastructure.persistence;

import com.deliveryhub.infrastructure.entity.user.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

/**
 * Repositorio de Spring Data JPA para realizar operaciones de persistencia en la tabla 'users'.
 */
public interface UserJpaRepository extends JpaRepository<UserEntity, UUID> {
    /**
     * Verifica si existe un UserEntity por correo electrónico.
     *
     * @param email el correo a buscar
     * @return true si existe, false en caso contrario
     */
    boolean existsByEmail(String email);

    /**
     * Verifica si existe un UserEntity por nombre.
     *
     * @param name el nombre a buscar
     * @return true si existe, false en caso contrario
     */
    boolean existsByName(String name);

    /**
     * Busca un UserEntity por correo electrónico.
     *
     * @param email el correo a buscar
     * @return un Optional con el UserEntity si se encuentra
     */
    java.util.Optional<UserEntity> findByEmail(String email);
}
