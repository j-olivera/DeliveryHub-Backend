package com.deliveryhub.infrastructure.security;

import com.deliveryhub.application.port.out.PasswordEncoderPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Adaptador de infraestructura para la codificación y verificación de contraseñas.
 * Implementa el puerto {@link PasswordEncoderPort} utilizando el BCryptPasswordEncoder provisto por Spring Security.
 */
@Component
public class PasswordEncoderAdapter implements PasswordEncoderPort {

    private final PasswordEncoder passwordEncoder;

    /**
     * Constructor del adaptador.
     *
     * @param passwordEncoder el codificador de contraseñas de Spring Security
     */
    public PasswordEncoderAdapter(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public String encode(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    @Override
    public boolean matches(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}
