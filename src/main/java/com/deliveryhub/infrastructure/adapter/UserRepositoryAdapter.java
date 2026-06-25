package com.deliveryhub.infrastructure.adapter;

import com.deliveryhub.application.port.out.user.UserRepositoryPort;
import com.deliveryhub.domain.model.User;
import com.deliveryhub.infrastructure.mapper.UserMapper;
import com.deliveryhub.infrastructure.entity.user.*;
import org.springframework.stereotype.Repository;
import com.deliveryhub.infrastructure.persistence.UserJpaRepository;

/**
 * Adaptador de persistencia para el repositorio de usuarios.
 * Implementa {@link UserRepositoryPort} para desacoplar el dominio de la infraestructura de base de datos.
 */
@Repository
public class UserRepositoryAdapter implements UserRepositoryPort {

    private final UserJpaRepository userJpaRepository;
    private final UserMapper userMapper;

    /**
     * Constructor para inyección de dependencias.
     *
     * @param userJpaRepository el repositorio de Spring Data JPA
     * @param userMapper el mapeador de entidad <-> dominio
     */
    public UserRepositoryAdapter(UserJpaRepository userJpaRepository, UserMapper userMapper) {
        this.userJpaRepository = userJpaRepository;
        this.userMapper = userMapper;
    }

    @Override
    public User save(User user) {
        UserEntity entity = userMapper.toEntity(user);
        UserEntity savedEntity = userJpaRepository.save(entity);
        return userMapper.toDomain(savedEntity);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userJpaRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByName(String name) {
        return userJpaRepository.existsByName(name);
    }

    @Override
    public java.util.Optional<User> findByEmail(String email) {
        return userJpaRepository.findByEmail(email)
                .map(userMapper::toDomain);
    }

    @Override
    public java.util.Optional<User> findById(java.util.UUID id) {
        return userJpaRepository.findById(id)
                .map(userMapper::toDomain);
    }
}
