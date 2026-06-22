package com.deliveryhub.application.port.out;

/**
 * Puerto de salida para las operaciones de codificación y verificación de contraseñas.
 */
public interface PasswordEncoderPort {
    /**
     * Codifica una contraseña en texto plano utilizando un algoritmo de hash seguro.
     *
     * @param rawPassword la contraseña en texto plano
     * @return la contraseña codificada
     */
    String encode(String rawPassword);

    /**
     * Verifica si una contraseña en texto plano coincide con una contraseña codificada.
     *
     * @param rawPassword la contraseña en texto plano
     * @param encodedPassword la contraseña codificada
     * @return true si coinciden, false en caso contrario
     */
    boolean matches(String rawPassword, String encodedPassword);
}
