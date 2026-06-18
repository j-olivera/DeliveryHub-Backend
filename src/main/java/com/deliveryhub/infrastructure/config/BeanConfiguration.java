package com.deliveryhub.infrastructure.config;

import com.deliveryhub.application.port.in.RegisterUserUseCase;
import com.deliveryhub.application.port.out.UserRepositoryPort;
import com.deliveryhub.application.usecase.RegisterUserUseCaseImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Clase de configuración para registrar los casos de uso como Beans en el contexto de Spring.
 * Esto mantiene la capa de aplicación libre de anotaciones de frameworks externos.
 */
@Configuration
public class BeanConfiguration {

    /**
     * Registra el caso de uso para el registro de usuarios.
     *
     * @param userRepositoryPort puerto de salida para la persistencia del usuario
     * @return la implementación del caso de uso RegisterUserUseCase
     */
    @Bean
    public RegisterUserUseCase registerUserUseCase(UserRepositoryPort userRepositoryPort) {
        return new RegisterUserUseCaseImpl(userRepositoryPort);
    }
}
