package com.deliveryhub.infrastructure.config;

import com.deliveryhub.application.port.in.RegisterUserUseCase;
import com.deliveryhub.application.port.out.UserRepositoryPort;
import com.deliveryhub.application.usecase.RegisterUserUseCaseImpl;
import com.deliveryhub.application.usecase.LoginUseCaseImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Clase de configuración para registrar los casos de uso como Beans en el contexto de Spring.
 * Esto mantiene la capa de aplicación libre de anotaciones de frameworks externos.
 */
@Configuration
public class BeanConfiguration {

    /**
     * Registra el codificador de contraseñas de Spring Security.
     *
     * @return BCryptPasswordEncoder
     */
    @Bean
    public org.springframework.security.crypto.password.PasswordEncoder passwordEncoder() {
        return new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
    }

    /**
     * Registra el caso de uso para el registro de usuarios.
     *
     * @param userRepositoryPort puerto de salida para la persistencia del usuario
     * @return la implementación del caso de uso RegisterUserUseCase
     */
    @Bean
    public RegisterUserUseCase registerUserUseCase(UserRepositoryPort userRepositoryPort,
                                                   com.deliveryhub.application.port.out.PasswordEncoderPort passwordEncoderPort) {
        return new RegisterUserUseCaseImpl(userRepositoryPort, passwordEncoderPort);
    }

    /**
     * Registra el caso de uso para el inicio de sesión de usuarios.
     *
     * @param userRepositoryPort  puerto de salida para la persistencia del usuario
     * @param passwordEncoderPort puerto de salida para la codificación/verificación de contraseñas
     * @param jwtUtils            utilidades de JWT
     * @return la implementación del caso de uso LoginUseCase
     */
    @Bean
    public com.deliveryhub.application.port.in.LoginUseCase loginUseCase(
            UserRepositoryPort userRepositoryPort,
            com.deliveryhub.application.port.out.PasswordEncoderPort passwordEncoderPort,
            com.deliveryhub.infrastructure.security.JwtUtils jwtUtils) {
        return new LoginUseCaseImpl(userRepositoryPort, passwordEncoderPort, jwtUtils);
    }
}
