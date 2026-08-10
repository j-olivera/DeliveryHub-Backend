package com.deliveryhub.infrastructure.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;
import java.util.UUID;

/**
 * Implementación personalizada de UserDetails que almacena el UUID de dominio del usuario.
 */
public class CustomUserDetails extends User {

    private final UUID id;

    /**
     * Constructor de CustomUserDetails.
     *
     * @param id el ID de usuario
     * @param username el nombre de usuario (correo electrónico)
     * @param password la contraseña
     * @param enabled si el usuario está activo
     * @param authorities las autoridades
     */
    public CustomUserDetails(UUID id, String username, String password, boolean enabled, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, enabled, true, true, true, authorities);
        this.id = id;
    }

    /**
     * Obtiene el ID del usuario.
     *
     * @return el ID del usuario
     */
    public UUID getId() {
        return id;
    }
}
