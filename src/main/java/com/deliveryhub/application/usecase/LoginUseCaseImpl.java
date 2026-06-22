package com.deliveryhub.application.usecase;

import com.deliveryhub.application.dto.LoginResponse;
import com.deliveryhub.application.port.in.LoginUseCase;
import com.deliveryhub.application.port.out.PasswordEncoderPort;
import com.deliveryhub.application.port.out.UserRepositoryPort;
import com.deliveryhub.domain.exception.InvalidCredentialsException;
import com.deliveryhub.domain.model.User;
import com.deliveryhub.infrastructure.security.JwtUtils;

/**
 * Implementación del caso de uso de inicio de sesión.
 */
public class LoginUseCaseImpl implements LoginUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordEncoderPort passwordEncoderPort;
    private final JwtUtils jwtUtils;

    /**
     * Constructor para inyección de dependencias.
     *
     * @param userRepositoryPort puerto de persistencia de usuarios
     * @param passwordEncoderPort puerto de codificación de contraseñas
     * @param jwtUtils utilidades para tokens JWT
     */
    public LoginUseCaseImpl(UserRepositoryPort userRepositoryPort,
                            PasswordEncoderPort passwordEncoderPort,
                            JwtUtils jwtUtils) {
        this.userRepositoryPort = userRepositoryPort;
        this.passwordEncoderPort = passwordEncoderPort;
        this.jwtUtils = jwtUtils;
    }

    @Override
    public LoginResponse execute(String email, String password) {
        User user = userRepositoryPort.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoderPort.matches(password, user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        String token = jwtUtils.generateToken(user.getEmail());

        return new LoginResponse(token, user.getId(), user.getRole().name());
    }
}
