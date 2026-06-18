package com.deliveryhub.presentation.rest;

import com.deliveryhub.application.port.in.RegisterUserUseCase;
import com.deliveryhub.domain.model.User;
import com.deliveryhub.presentation.dto.RegisterUserRequest;
import com.deliveryhub.presentation.dto.UserResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para la gestión de usuarios.
 * Expone endpoints relacionados con operaciones de usuarios, como el registro.
 */
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final RegisterUserUseCase registerUserUseCase;

    /**
     * Constructor del controlador.
     *
     * @param registerUserUseCase caso de uso de registro de usuario
     */
    public UserController(RegisterUserUseCase registerUserUseCase) {
        this.registerUserUseCase = registerUserUseCase;
    }

    /**
     * Registra un nuevo usuario en el sistema.
     *
     * @param request DTO de solicitud de registro
     * @return DTO de respuesta con los datos del usuario registrado y estado 201 Created
     */
    @PostMapping
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody RegisterUserRequest request) {
        com.deliveryhub.application.dto.RegisterUserRequest applicationRequest =
            new com.deliveryhub.application.dto.RegisterUserRequest(
                request.name(),
                request.email(),
                request.password(),
                request.role()
            );

        User createdUser = registerUserUseCase.execute(applicationRequest);

        UserResponse response = new UserResponse(
            createdUser.getId(),
            createdUser.getName(),
            createdUser.getEmail(),
            createdUser.getRole().name()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
