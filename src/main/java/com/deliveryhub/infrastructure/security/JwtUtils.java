package com.deliveryhub.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Utilidades para la generación y validación de tokens JWT.
 */
@Component
public class JwtUtils {

    private final SecretKey key;
    private final int jwtExpirationMs;

    /**
     * Constructor de JwtUtils.
     *
     * @param jwtSecret la clave secreta utilizada para firmar el token
     * @param jwtExpirationMs el tiempo de expiración del token en milisegundos
     */
    public JwtUtils(
            @Value("${security.jwt.secret:mySuperSecretKeyForJwtGenerationMustBeAtLeast256BitsLong!}") String jwtSecret,
            @Value("${security.jwt.expiration-ms:86400000}") int jwtExpirationMs) {
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        this.jwtExpirationMs = jwtExpirationMs;
    }

    /**
     * Genera un token JWT para el nombre de usuario especificado.
     *
     * @param username el nombre de usuario (subject del token)
     * @return el token JWT codificado
     */
    public String generateToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    /**
     * Extrae el nombre de usuario (subject) de un token JWT.
     *
     * @param token el token JWT
     * @return el nombre de usuario extraído
     */
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.getSubject();
    }

    /**
     * Valida la firma y expiración de un token JWT.
     *
     * @param token el token JWT a validar
     * @return true si el token es válido, false en caso contrario
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // Log de depuración o manejo silencioso de firma inválida / expirada
            return false;
        }
    }
}
