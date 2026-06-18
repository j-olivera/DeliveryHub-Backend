package com.deliveryhub.application.usecase;

import com.deliveryhub.application.dto.RegisterUserRequest;
import com.deliveryhub.application.port.in.RegisterUserUseCase;
import com.deliveryhub.application.port.out.UserRepositoryPort;
import com.deliveryhub.domain.enums.UserRole;
import com.deliveryhub.domain.exception.DomainValidationException;
import com.deliveryhub.domain.exception.UserAlreadyExistsException;
import com.deliveryhub.domain.model.User;

/**
 * Implementación del caso de uso para registrar nuevos usuarios.
 */
public class RegisterUserUseCaseImpl implements RegisterUserUseCase {

    private final UserRepositoryPort userRepositoryPort;

    /**
     * Construye una nueva instancia del caso de uso con el puerto de persistencia inyectado.
     *
     * @param userRepositoryPort el puerto de persistencia de usuarios
     */
    public RegisterUserUseCaseImpl(UserRepositoryPort userRepositoryPort) {
        this.userRepositoryPort = userRepositoryPort;
    }

    /**
     * Registra un nuevo usuario tras validar sus datos y unicidad.
     *
     * @param request datos del usuario
     * @return usuario creado y persistido
     */
    @Override
    public User execute(RegisterUserRequest request) {
        // Mapear y validar el rol solicitado
        UserRole userRole = parseRole(request.role());

        // Instanciación del dominio primero para validación de formato
        User user = User.create(
            request.name(),
            request.email(),
            request.password(),
            userRole
        );

        // Control de unicidad contra el repositorio
        checkUniqueness(user.getEmail(), user.getName());

        // Persistir y retornar el usuario registrado
        return userRepositoryPort.save(user);
    }

    private void checkUniqueness(String email, String name) {
        if (userRepositoryPort.existsByEmail(email)) {
            throw new UserAlreadyExistsException("User with email " + email + " already exists");
        }
        if (userRepositoryPort.existsByName(name)) {
            throw new UserAlreadyExistsException("User with name " + name + " already exists");
        }
    }

    private UserRole parseRole(String roleName) {
        if (roleName == null) {
            throw new DomainValidationException("Role is mandatory");
        }
        try {
            return UserRole.valueOf(roleName);
        } catch (IllegalArgumentException e) {
            throw new DomainValidationException("Invalid role");
        }
    }
}
