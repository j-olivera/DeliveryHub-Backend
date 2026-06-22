package com.deliveryhub.domain.exception;

/**
 * Excepción lanzada cuando las credenciales ingresadas son inválidas (correo no existe o contraseña incorrecta).
 */
public class InvalidCredentialsException extends DomainValidationException {
    public InvalidCredentialsException(String message) {
        super(message);
    }
}
