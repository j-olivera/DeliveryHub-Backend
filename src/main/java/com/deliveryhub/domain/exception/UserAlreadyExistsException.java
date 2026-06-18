package com.deliveryhub.domain.exception;

/**
 * Excepción lanzada cuando se intenta registrar un usuario con un correo o nombre que ya existen.
 */
public class UserAlreadyExistsException extends DomainValidationException {
    /**
     * Crea una nueva excepción con el mensaje especificado.
     *
     * @param message el mensaje de error
     */
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
