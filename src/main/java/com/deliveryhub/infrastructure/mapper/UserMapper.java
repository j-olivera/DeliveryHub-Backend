package com.deliveryhub.infrastructure.mapper;

import com.deliveryhub.domain.model.User;
import com.deliveryhub.infrastructure.entity.user.*;
import org.springframework.stereotype.Component;

/**
 * Mapper manual para mapear entre la entidad de dominio User y la entidad JPA UserEntity.
 */
@Component
public class UserMapper {

    /**
     * Mapea un UserEntity a la entidad de dominio User.
     *
     * @param entity la entidad JPA a mapear
     * @return la entidad de dominio User
     */
    public User toDomain(UserEntity entity) {
        if (entity == null) {
            return null;
        }
        return User.reconstruct(
            entity.getId(),
            entity.getName(),
            entity.getEmail(),
            entity.getPassword(),
            entity.getRole()
        );
    }

    /**
     * Mapea la entidad de dominio User a un UserEntity.
     *
     * @param domain el usuario de dominio a mapear
     * @return la entidad JPA UserEntity
     */
    public UserEntity toEntity(User domain) {
        if (domain == null) {
            return null;
        }
        UserEntity entity = new UserEntity();
        entity.setId(domain.getId());
        entity.setName(domain.getName());
        entity.setEmail(domain.getEmail());
        entity.setPassword(domain.getPassword());
        entity.setRole(domain.getRole());
        return entity;
    }
}
