package com.deliveryhub.presentation.rest;

import com.deliveryhub.domain.exception.DomainValidationException;
import com.deliveryhub.domain.exception.UserAlreadyExistsException;
import com.deliveryhub.presentation.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * Controlador de consejos globales para el manejo de excepciones en la API.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Maneja excepciones de tipo UserAlreadyExistsException (usuario duplicado).
     * Retorna un estado HTTP 409 Conflict junto con un DTO ErrorResponse.
     *
     * @param ex excepción capturada
     * @return respuesta de error formateada con código HTTP 409
     */
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExistsException(UserAlreadyExistsException ex) {
        ErrorResponse error = new ErrorResponse(ex.getMessage(), LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    /**
     * Maneja excepciones de validación de argumentos de métodos (JSR-380 @Valid).
     * Retorna un estado HTTP 400 Bad Request junto con un DTO ErrorResponse conteniendo los mensajes de error.
     *
     * @param ex excepción capturada
     * @return respuesta de error formateada con código HTTP 400
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.joining(", "));
        ErrorResponse error = new ErrorResponse(message, LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Maneja excepciones de validación de reglas de negocio del dominio.
     * Retorna un estado HTTP 400 Bad Request junto con un DTO ErrorResponse.
     *
     * @param ex excepción capturada
     * @return respuesta de error formateada con código HTTP 400
     */
    @ExceptionHandler(DomainValidationException.class)
    public ResponseEntity<ErrorResponse> handleDomainValidationException(DomainValidationException ex) {
        ErrorResponse error = new ErrorResponse(ex.getMessage(), LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Maneja excepciones de credenciales inválidas.
     * Retorna un estado HTTP 401 Unauthorized junto con un DTO ErrorResponse.
     *
     * @param ex excepción capturada
     * @return respuesta de error formateada con código HTTP 401
     */
    @ExceptionHandler(com.deliveryhub.domain.exception.InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentialsException(com.deliveryhub.domain.exception.InvalidCredentialsException ex) {
        ErrorResponse error = new ErrorResponse(ex.getMessage(), LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }
}
