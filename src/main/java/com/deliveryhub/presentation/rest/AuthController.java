package com.deliveryhub.presentation.rest;

import com.deliveryhub.application.dto.LoginResponse;
import com.deliveryhub.application.port.in.LoginUseCase;
import com.deliveryhub.presentation.dto.LoginRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para el proceso de autenticación.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final LoginUseCase loginUseCase;

    /**
     * Constructor del controlador.
     *
     * @param loginUseCase caso de uso de inicio de sesión
     */
    public AuthController(LoginUseCase loginUseCase) {
        this.loginUseCase = loginUseCase;
    }

    /**
     * Inicia sesión para un usuario en el sistema.
     *
     * @param request DTO con las credenciales de inicio de sesión
     * @return respuesta HTTP con el DTO LoginResponse
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = loginUseCase.execute(request.email(), request.password());
        return ResponseEntity.ok(response);
    }
}
