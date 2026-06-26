package com.deliveryhub.presentation.rest.user;

import com.deliveryhub.application.port.in.user.RegisterUserUseCase;
import com.deliveryhub.domain.model.User;
import com.deliveryhub.presentation.dto.user.RegisterUserRequest;
import com.deliveryhub.presentation.dto.user.UserResponse;
import com.deliveryhub.presentation.mapper.user.UserPresentationMapper;
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
    private final com.deliveryhub.application.port.in.user.GetActiveDeliveriesUseCase getActiveDeliveriesUseCase;
    private final UserPresentationMapper userPresentationMapper;

    /**
     * Constructor del controlador.
     *
     * @param registerUserUseCase caso de uso de registro de usuario
     * @param getActiveDeliveriesUseCase caso de uso para listar repartidores activos
     * @param userPresentationMapper mapeador de la capa de presentación
     */
    public UserController(RegisterUserUseCase registerUserUseCase,
                          com.deliveryhub.application.port.in.user.GetActiveDeliveriesUseCase getActiveDeliveriesUseCase,
                          UserPresentationMapper userPresentationMapper) {
        this.registerUserUseCase = registerUserUseCase;
        this.getActiveDeliveriesUseCase = getActiveDeliveriesUseCase;
        this.userPresentationMapper = userPresentationMapper;
    }

    /**
     * Registra un nuevo usuario en el sistema.
     *
     * @param request DTO de solicitud de registro
     * @return DTO de respuesta con los datos del usuario registrado y estado 201 Created
     */
    @PostMapping
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody RegisterUserRequest request) {
        com.deliveryhub.application.dto.user.RegisterUserRequest applicationRequest =
            userPresentationMapper.toApplicationRequest(request);

        User createdUser = registerUserUseCase.execute(applicationRequest);

        UserResponse response = userPresentationMapper.toResponse(createdUser);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Obtiene la lista de repartidores activos.
     *
     * @return lista de repartidores y estado 200 OK
     */
    @org.springframework.web.bind.annotation.GetMapping
    public ResponseEntity<java.util.List<UserResponse>> getActiveDeliveries() {
        java.util.List<User> riders = getActiveDeliveriesUseCase.execute();
        java.util.List<UserResponse> response = riders.stream()
                .map(userPresentationMapper::toResponse)
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(response);
    }
}
