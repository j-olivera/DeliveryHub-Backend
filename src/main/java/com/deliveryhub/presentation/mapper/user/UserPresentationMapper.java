package com.deliveryhub.presentation.mapper.user;

import com.deliveryhub.domain.model.User;
import com.deliveryhub.presentation.dto.user.RegisterUserRequest;
import com.deliveryhub.presentation.dto.user.UserResponse;
import org.springframework.stereotype.Component;

/**
 * Mapeador manual para la capa de presentación.
 * Se encarga de convertir entre DTOs de presentación, DTOs de aplicación y entidades de dominio.
 */
@Component
public class UserPresentationMapper {

    /**
     * Mapea un DTO de presentación RegisterUserRequest a un DTO de aplicación RegisterUserRequest.
     *
     * @param request el DTO de presentación con los datos de registro
     * @return el DTO de aplicación para el caso de uso
     */
    public com.deliveryhub.application.dto.user.RegisterUserRequest toApplicationRequest(RegisterUserRequest request) {
        if (request == null) {
            return null;
        }
        return new com.deliveryhub.application.dto.user.RegisterUserRequest(
            request.name(),
            request.email(),
            request.password(),
            request.role()
        );
    }

    /**
     * Mapea una entidad de dominio User a un DTO de respuesta UserResponse.
     *
     * @param domain la entidad de dominio User
     * @return el DTO de respuesta UserResponse
     */
    public UserResponse toResponse(User domain) {
        if (domain == null) {
            return null;
        }
        return new UserResponse(
            domain.getId(),
            domain.getName(),
            domain.getEmail(),
            domain.getRole().name()
        );
    }
}
